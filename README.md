![效果图](https://github.com/huisonma/EasyTheme/blob/master/sample.gif)



支持Apk、Zip切换皮肤
支持background、textColor、typeface、textSize等属性

# Skin Apply

1.lightmode、nightmode为Apk皮肤，如正常Apk安装即可

2./download_skin/ 文件下为zip皮肤文件，修改完可以通过build.sh脚本压缩后写入设备 /sdcard/Android/data/{package_name}/files/skin 文件夹下，运行demo Apk即可

# Code Use

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	    implementation 'com.github.huisonma:EasyTheme:1.0.3'
	}
