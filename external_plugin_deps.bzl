load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
  POWER_MOCK_VERSION = "1.6.2"

  maven_jar(
    name = "powermock_module_junit4",
    artifact = "org.powermock:powermock-module-junit4:" + POWER_MOCK_VERSION,
    sha1 = "dff58978da716e000463bc1b08013d6a7cf3d696",
  )

  maven_jar(
    name = "powermock_api_mockito",
    artifact = "org.powermock:powermock-api-mockito:" + POWER_MOCK_VERSION,
    sha1 = "c213230ae20a7b422f3d622a261d0e3427d2464c",
  )

  maven_jar(
    name = "mockito",
    artifact = "org.mockito:mockito-all:1.10.19",
    sha1 = "539df70269cc254a58cccc5d8e43286b4a73bf30",
  )
