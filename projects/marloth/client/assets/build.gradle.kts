import mythic.gradle.assets.svg.SvgTask
import mythic.gradle.assets.general.ModelAssetsTask
import mythic.gradle.assets.general.TextureAssetsTask
import mythic.gradle.assets.general.AudioAssetsTask

buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath("mythic.gradle.assets.general:1.0")
    classpath("mythic.gradle.assets.svg:1.0")
  }
}

val assetsDir = project.projectDir.toString().replace("\\", "/")

tasks.create<ModelAssetsTask>("modelAssets") {
  inputDir.set(file("$assetsDir/blend/models"))
  outputDir.set(file("$assetsDir/src/main/resources/models"))
  executablePath.set(getRequiredConfigValue("BLENDER_PATH"))
  projectDir.set(assetsDir)
}

tasks.create<TextureAssetsTask>("textureAssets") {
  inputDir.set(file("$assetsDir/textures"))
  outputDir.set(file("$assetsDir/src/main/resources/textures"))
  executablePath.set(getRequiredConfigValue("PYTHON_PATH"))
  projectDir.set(assetsDir)
}

tasks.create<AudioAssetsTask>("audioAssets") {
  inputDir.set(file("$assetsDir/audio/supercollider/sounds"))
  outputDir.set(file("$assetsDir/src/main/resources/audio"))
  executablePath.set(getRequiredConfigValue("PYTHON_PATH"))
//  sclangPath.set(getRequiredConfigValue("SCLANG_PATH"))
//  scsynthPath.set(getRequiredConfigValue("SCSYNTH_PATH"))
//  oggencPath.set(getRequiredConfigValue("OGGENC_PATH"))
  projectDir.set(assetsDir)
}

tasks.create<SvgTask>("imageAssets") {
  //  dependsOn textureAssets
  inputDir.set(file("$projectDir/svg"))
  outputDir.set(file("$projectDir/src/main/resources/textures"))
  executablePath.set(getRequiredConfigValue("INKSCAPE_PATH"))
}

//project.classes {
//  dependsOn modelAssets28
//  dependsOn textureAssets
//  dependsOn audioAssets
//  dependsOn imageAssets
//}
