buildscript {
  repositories {
    mavenLocal()
    maven {
      url = uri("https://plugins.gradle.org/m2/")
      name = "GradlePlugin"
    }
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20-M1")
  }
}

group = "com.tabuyos.kotlinlearn"
version = "1.0"

val removeParentSrc = true

fun deleteDirectory(file: File): Boolean {
  if (file.isFile) {
    return file.delete()
  } else {
    val lfs = file.listFiles() ?: return true
    lfs.forEach {
      deleteDirectory(it)
    }
  }
  return file.delete()
}

fun automaticCreateModule(project: Project) {
  // 获取项目的 sourceSets 资源
  val sourceSets = project.extensions.getByType(SourceSetContainer::class)

  // 自动生成相关目录(自动建 module)
  sourceSets
    .forEach { typeIt ->
      typeIt
        .allSource
        .srcDirs
        .forEach { srcIt ->
          srcIt.mkdirs()
        }
    }

  // 创建构建脚本文件
  var buildFile = file("${project.projectDir}${File.separator}build.gradle.kts")
  if (!buildFile.exists()) {
    buildFile.createNewFile()
  }

  // 移除父级 module 的 src 目录
  if (removeParentSrc) {
    buildFile = file("${buildFile.parentFile.parent}${File.separator}src")
    if (buildFile.exists()) {
      deleteDirectory(buildFile)
    }
  }
}

allprojects {
  repositories {
    mavenLocal()
    maven {
      url = uri("https://maven.aliyun.com/repository/public")
      name = "AliyunPublic"
    }
    mavenCentral()
  }
}

subprojects {
  // 添加默认 kotlin jvm plugin
  apply(plugin = "org.jetbrains.kotlin.jvm")

  // 定义相关变量
  val implementation by configurations

  // 添加默认依赖
  dependencies {
    implementation(kotlin("stdlib"))
  }

  // 自动创建
  automaticCreateModule(project)

  tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
      suppressWarnings = true
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }

}