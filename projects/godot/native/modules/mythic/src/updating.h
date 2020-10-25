#pragma once
#include "core/message_queue.h"

// main.cpp injection functions
MessageQueue * get_message_queue();
MainTimerSync& get_main_timer_sync();
int get_fixed_fps();
uint64_t get_physics_process_max();
void set_physics_process_max(uint64_t value);
uint64_t get_idle_process_max();
void set_idle_process_max(uint64_t value);
