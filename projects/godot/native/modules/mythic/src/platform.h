#pragma once

#include <memory>

#include "core/os/os.h"

class Platform {
public:
	virtual OS* getOs() = 0;
	virtual void pumpEvents() = 0;
	virtual int start() = 0;
	virtual int stop() = 0;
};
