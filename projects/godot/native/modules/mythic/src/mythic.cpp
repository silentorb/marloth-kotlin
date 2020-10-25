#pragma once

#include "main/main.h"
#include "platform/windows/os_windows.h"
#include "mythic_windows.h"

#include <locale.h>

extern "C" {

	_declspec(dllexport) WindowsPlatform* newWindowsPlatform() {
		return new WindowsPlatform();
	}

	_declspec(dllexport) void deleteWindowsPlatform(WindowsPlatform* platform) {
		if (platform) {
			delete platform;
		}
	}

	_declspec(dllexport) int startGodot(Platform *platform, const char* execpath, int argc, char* argv[]) {

		setlocale(LC_CTYPE, "");

		Error err = Main::setup(execpath, argc, argv);

		int startResult = Main::start();
		if (!startResult)
			return startResult;

		return platform->start();
		
		//os->mainLoop.init();

					//os->run();

	}

	_declspec(dllexport) void pumpEvents(Platform* platform) {
		platform->pumpEvents();
	}

	_declspec(dllexport) int stopGodot(Platform* platform) {

		//	mainLoop.finish();
		platform->stop();
		Main::cleanup();
		return platform->getOs()->get_exit_code();
	}
}
