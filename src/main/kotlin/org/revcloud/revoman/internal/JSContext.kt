package org.revcloud.revoman.internal

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.http4k.core.Response
import org.revcloud.revoman.internal.postman.pm
import org.revcloud.revoman.internal.postman.state.Item
import org.revcloud.revoman.internal.postman.state.Request

internal val jsContext = buildJsContext(false).also {
  it.getBindings("js").putMember("pm", pm)
  it.getBindings("js").putMember("xml2Json", pm.xml2Json)
}

private fun buildJsContext(useCommonjsRequire: Boolean = true): Context {
  val options = buildMap {
    if (useCommonjsRequire) {
      put("js.commonjs-require", "true")
      put("js.commonjs-require-cwd", ".")
      put("js.commonjs-core-modules-replacements", "path:path-browserify")
    }
    put("js.esm-eval-returns-exports", "true")
    put("engine.WarnInterpreterOnly", "false")
  }
  return Context.newBuilder("js")
    .allowExperimentalOptions(true)
    .allowIO(true)
    .options(options)
    .allowHostAccess(HostAccess.ALL)
    .allowHostClassLookup { true }
    .build()
}

internal fun executeTestScriptJs(
  step: Item,
  response: Response
) {
  // ! TODO 12/03/23 gopala.akshintala: Find a way to surface-up what happened in the script, like the Ids set etc 
  loadIntoPmEnvironment(step.request, response)
  val testScript = step.event?.find { it.listen == "test" }?.script?.exec?.joinToString("\n")
  if (!testScript.isNullOrBlank()) {
    val testSource = Source.newBuilder("js", testScript, "pmItemTestScript.js").build()
    jsContext.getBindings("js").putMember("responseBody", response.bodyString())
    // ! TODO gopala.akshintala 15/05/22: Keep a tab on jsContext mix-up from different steps
    jsContext.eval(testSource)
  }
}

private fun loadIntoPmEnvironment(stepRequest: Request, response: Response) {
  pm.request = stepRequest
  pm.response = org.revcloud.revoman.internal.postman.Response(
    response.status.toString(),
    response.status.code.toString(),
    response.bodyString()
  )
}