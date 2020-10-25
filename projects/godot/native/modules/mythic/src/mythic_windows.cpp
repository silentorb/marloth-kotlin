#include "platform.h"
#include "mythic_windows.h"

OS* WindowsPlatform::getOs() {
	return this;
}

void WindowsPlatform::pumpEvents() {
	this->process_events();
}

int WindowsPlatform::start() {
	get_main_loop()->init();
	return 0;
}

int WindowsPlatform::stop() {
	get_main_loop()->finish();
	return 0;
}
