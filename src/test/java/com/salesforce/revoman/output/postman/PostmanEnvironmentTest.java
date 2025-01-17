/*******************************************************************************
 * Copyright (c) 2023, Salesforce, Inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.revoman.output.postman;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostmanEnvironmentTest {
  @Test
  @DisplayName("Multi level filtering")
  void multiLevelFiltering() {
    final var postmanEnv =
        new PostmanEnvironment<>(
            Map.of(
                "standardPricebookId", "fakeStandardPricebookId",
                "accessToken", "fakeAccessToken",
                "salesRepPsgId", "fakePsgId",
                "salesRepUserId", "fakeUserId",
                "mockTaxAdapterId", "mockTaxAdapterId"));
    final var filteredEnv =
        postmanEnv
            .mutableEnvCopyExcludingKeys(String.class, Set.of("standardPricebookId"))
            .mutableEnvCopyWithKeysEndingWith(String.class, "Id")
            .mutableEnvCopyWithKeysNotEndingWith(String.class, "PsgId", "UserId");
    assertThat(filteredEnv)
        .containsExactlyInAnyOrderEntriesOf(Map.of("mockTaxAdapterId", "mockTaxAdapterId"));
  }
}
