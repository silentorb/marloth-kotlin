#pragma once

#include "platform.h"
#include "platform/windows/os_windows.h"

class WindowsPlatform : public Platform, OS_Windows {

public:
	WindowsPlatform() : OS_Windows { nullptr } {}

	virtual OS* getOs();
	virtual void pumpEvents();
	virtual int start();
	virtual int stop();
};
