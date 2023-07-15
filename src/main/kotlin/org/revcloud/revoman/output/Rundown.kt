package org.revcloud.revoman.output

import java.lang.reflect.Type
import org.http4k.core.Request
import org.http4k.core.Response
import org.revcloud.revoman.postman.PostmanEnvironment

data class Rundown(
  @JvmField val stepNameToReport: Map<String, StepReport> = emptyMap(),
  @JvmField val environment: PostmanEnvironment = PostmanEnvironment()
) {
  val firstUnsuccessfulStepNameInOrder: String?
    get() =
      stepNameToReport.entries.firstOrNull { (_, stepReport) -> !stepReport.isSuccessful }?.key

  fun reportForStepName(stepName: String): StepReport? =
    stepNameToReport.entries
      .firstOrNull { it.key == stepName || it.key.substringAfterLast(FOLDER_DELIMITER) == stepName }
      ?.value
}

data class StepReport(
  val requestData: Request,
  val responseObj: Any? = null,
  val responseType: Type? = null,
  val responseData: Response? = null,
  val httpFailure: Throwable? = null,
  val testScriptJsFailure: Throwable? = null,
  val validationFailure: Any? = null
) {
  val isSuccessful: Boolean
    get() =
      (responseData?.status?.successful
        ?: false) && validationFailure == null && testScriptJsFailure == null
}

const val FOLDER_DELIMITER = "|>"
