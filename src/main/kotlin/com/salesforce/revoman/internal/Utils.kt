/**
 * ****************************************************************************
 * Copyright (c) 2023, Salesforce, Inc. All rights reserved. SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.revoman.internal

import com.google.common.io.Resources
import com.salesforce.revoman.input.HookConfig
import com.salesforce.revoman.input.HookConfig.Hook
import com.salesforce.revoman.input.HookConfig.HookType
import com.salesforce.revoman.input.RequestConfig
import com.salesforce.revoman.input.ResponseConfig
import com.salesforce.revoman.internal.postman.state.Auth
import com.salesforce.revoman.internal.postman.state.Item
import com.salesforce.revoman.output.FOLDER_DELIMITER
import com.salesforce.revoman.output.HTTP_METHOD_SEPARATOR
import io.vavr.control.Either
import org.apache.commons.lang3.StringUtils
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.HttpMessage

internal fun isContentTypeApplicationJson(httpMessage: HttpMessage) =
  httpMessage.bodyString().isNotBlank() &&
    httpMessage.header("content-type")?.let {
      StringUtils.deleteWhitespace(it)
        .equals(StringUtils.deleteWhitespace(APPLICATION_JSON.toHeaderValue()), ignoreCase = true)
    }
      ?: false

internal fun readFileToString(fileRelativePath: String): String =
  Resources.getResource(fileRelativePath).readText()

internal fun List<Item>.deepFlattenItems(
  parentFolderName: String = "",
  parentIndex: String = "",
  authFromRoot: Auth? = null
): List<Item> =
  asSequence()
    .flatMapIndexed { itemIndex, item ->
      val concatWithParentFolder =
        if (parentFolderName.isBlank()) item.name
        else "$parentFolderName$FOLDER_DELIMITER${item.name}"
      val index = if (parentIndex.isBlank()) "${itemIndex + 1}" else "$parentIndex.${itemIndex + 1}"
      item.item
        ?.map { childItem -> childItem.copy(auth = childItem.auth ?: item.auth ?: authFromRoot) }
        ?.deepFlattenItems(concatWithParentFolder, index)
        ?: listOf(
          item.copy(
            name = "$index -> ${item.request.method}$HTTP_METHOD_SEPARATOR$concatWithParentFolder",
            auth = item.auth ?: authFromRoot
          )
        )
    }
    .toList()

internal inline fun <reified T : Hook> getHooksForStep(
  currentStepName: String,
  hookType: HookType,
  hookConfigs: Map<HookType, List<HookConfig>>,
): List<T> =
  hookConfigs[hookType]
    ?.filter { stepNameEquals(it.stepName, currentStepName) }
    ?.map { it.hook as T }
    ?: emptyList()

internal fun stepNameVariants(stepName: String): Set<String> = buildSet {
  add(stepName)
  add(stepName.substringAfterLast(FOLDER_DELIMITER))
  if (!stepName.contains(FOLDER_DELIMITER)) add(stepName.substringAfterLast(HTTP_METHOD_SEPARATOR))
}

internal fun stepNameEquals(stepNameFromConfig: String, currentStepName: String) =
  stepNameFromConfig == currentStepName ||
    stepNameFromConfig == currentStepName.substringAfterLast(FOLDER_DELIMITER)

internal fun getResponseConfigForStepName(
  stepName: String,
  httpStatus: Boolean,
  stepNameToResponseConfig: Map<Pair<Boolean, String>, ResponseConfig>
): ResponseConfig? =
  stepNameVariants(stepName).firstNotNullOfOrNull { stepNameToResponseConfig[httpStatus to it] }

internal fun getRequestConfigForStepName(
  stepName: String,
  stepNameToRequestConfig: Map<String, RequestConfig>
): RequestConfig? = stepNameVariants(stepName).firstNotNullOfOrNull { stepNameToRequestConfig[it] }

internal fun isStepNameInPassList(stepName: String, haltOnAnyFailureExceptForSteps: Set<String>) =
  haltOnAnyFailureExceptForSteps.isEmpty() ||
    haltOnAnyFailureExceptForSteps.contains(stepName) ||
    haltOnAnyFailureExceptForSteps.contains(stepName.substringAfterLast(FOLDER_DELIMITER))

// ! TODO 24/06/23 gopala.akshintala: Regex support to filter Step Names
internal fun shouldStepBeExecuted(
  runOnlySteps: Set<String>,
  skipSteps: Set<String>,
  stepName: String
) =
  (runOnlySteps.isEmpty() && skipSteps.isEmpty()) ||
    (runOnlySteps.isNotEmpty() &&
      (runOnlySteps.contains(stepName) ||
        runOnlySteps.contains(stepName.substringAfterLast(FOLDER_DELIMITER))) ||
      (skipSteps.isNotEmpty() &&
        (!skipSteps.contains(stepName) &&
          !skipSteps.contains(stepName.substringAfterLast(FOLDER_DELIMITER)))))

internal fun <L, R> arrow.core.Either<L, R>.toVavr(): Either<L, R> =
  fold({ Either.left(it) }, { Either.right(it) })

internal fun <L, R> Either<L, R>.toArrow(): arrow.core.Either<L, R> =
  fold({ arrow.core.Either.Left(it) }, { arrow.core.Either.Right(it) })
