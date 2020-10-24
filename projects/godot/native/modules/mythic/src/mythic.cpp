#pragma once

#include "main/main.h"
#include "platform/windows/os_windows.h"

#include <locale.h>

extern "C" {

_declspec(dllexport)
int mythicMain(const char *execpath, int argc, char *argv[]) {

  OS_Windows os(NULL);

  setlocale(LC_CTYPE, "");

  Error err = Main::setup(execpath, argc, argv);

  if (Main::start())
    os.run();

  Main::cleanup();

  return os.get_exit_code();
}

}
