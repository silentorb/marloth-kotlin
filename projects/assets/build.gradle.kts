//import mythic.gradle.assets.general.ModelAssetsTask
//import mythic.gradle.assets.general.TextureAssetsTask
//import mythic.gradle.assets.general.AudioAssetsTask
//import mythic.gradle.assets.texturing.TexturingTask

//buildscript {
//  repositories {
//    jcenter()
//  }
//  dependencies {
////    classpath("silentorb.mythic.gradle.assets:general")
////    classpath("silentorb.mythic.gradle.assets:texturing")
////    implementation("silentorb.imp:parsing")
////    implementation("silentorb.imp:execution")
////    implementation("silentorb.imp:libraries_standard")
////    implementation("silentorb.imp:libraries_standard_implementation")
////    implementation("silentorb.mythic:imaging")
//  }
//}

dependencies {
    implementation("silentorb.imp:parsing")
    implementation("silentorb.imp:execution")
    implementation("silentorb.imp:libraries_standard")
    implementation("silentorb.imp:libraries_standard_implementation")
    implementation("silentorb.mythic:imaging")
}

val assetsDir = project.projectDir.toString().replace("\\", "/")

//tasks.create<ModelAssetsTask>("modelAssets") {
//  inputDir.set(file("$assetsDir/blend/models"))
//  outputDir.set(file("$assetsDir/src/main/resources/models"))
//  executablePath.set(getRequiredConfigValue("BLENDER_PATH"))
//  projectDir.set(assetsDir)
//}

//tasks.create<TextureAssetsTask>("textureAssets") {
//  inputDir.set(file("$assetsDir/textures"))
//  outputDir.set(file("$assetsDir/src/main/resources/textures"))
//  executablePath.set(getRequiredConfigValue("PYTHON_PATH"))
//  projectDir.set(assetsDir)
//}

//tasks.create<AudioAssetsTask>("audioAssets") {
//  inputDir.set(file("$assetsDir/audio/supercollider/sounds"))
//  outputDir.set(file("$assetsDir/src/main/resources/audio"))
//  executablePath.set(getRequiredConfigValue("PYTHON_PATH"))
////  sclangPath.set(getRequiredConfigValue("SCLANG_PATH"))
////  scsynthPath.set(getRequiredConfigValue("SCSYNTH_PATH"))
////  oggencPath.set(getRequiredConfigValue("OGGENC_PATH"))
//  projectDir.set(assetsDir)
//}

//tasks.create<TexturingTask>("textureAssets") {
//  inputDir.set(file("$projectDir/textures"))
//  outputDir.set(file("$projectDir/src/main/resources/textures"))
//}
