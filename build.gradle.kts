/**
 * ****************************************************************************
 * Copyright (c) 2023, Salesforce, Inc. All rights reserved. SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
plugins {
  id("revoman.root-conventions")
  id("revoman.publishing-conventions")
  id("revoman.kt-conventions")
  id(libs.plugins.kover.pluginId)
  alias(libs.plugins.nexus.publish)
  alias(libs.plugins.moshix)
}

dependencies {
  api(libs.bundles.http4k)
  api(libs.moshix.adapters)
  api(libs.bundles.vador)
  api(libs.arrow.core)
  implementation(libs.bundles.kotlin.logging)
  implementation(libs.apache.commons.lang3)
  implementation(libs.graal.sdk)
  implementation(libs.graal.js)
  implementation(libs.kotlin.faker)
  implementation(libs.underscore)
  implementation(libs.guava)
  implementation(libs.spring.beans)
  kapt(libs.immutables.value)
  compileOnly(libs.immutables.builder)
  compileOnly(libs.immutables.value.annotations)
  compileOnly(libs.jetbrains.annotations)
  testImplementation(libs.assertj.core)
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter(libs.versions.junit.get()) }
    val integrationTest by
      registering(JvmTestSuite::class) {
        dependencies {
          implementation(project())
          implementation(libs.assertj.core)
          implementation(libs.mockito.core)
          implementation(libs.spring.beans)
        }
      }
  }
}

koverReport { defaults { xml { onCheck = true } } }

moshi { enableSealed = true }

nexusPublishing { this.repositories { sonatype { stagingProfileId = STAGING_PROFILE_ID } } }
