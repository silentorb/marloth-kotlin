#include "main/main.h"
#include "core/os/os.h"
#include "servers/physics_server.h"
#include "main/main_timer_sync.h"
#include "servers/audio_server.h"
#include "servers/visual_server.h"
#include "updating.h"

static int static_physics_steps;
static float static_frame_slice;
static double static_step;
static float static_time_scale;
static double static_scaled_step;
static uint64_t static_ticks;

extern "C" {

	_declspec(dllexport) void updateTiming() {
		Main::iterating++;

		uint64_t ticks = OS::get_singleton()->get_ticks_usec();
		Engine::get_singleton()->_frame_ticks = ticks;
		get_main_timer_sync().set_cpu_ticks_usec(ticks);
		get_main_timer_sync().set_fixed_fps(get_fixed_fps());

		uint64_t ticks_elapsed = ticks - Main::last_ticks;

		int physics_fps = Engine::get_singleton()->get_iterations_per_second();
		float frame_slice = 1.0 / physics_fps;

		float time_scale = Engine::get_singleton()->get_time_scale();

		MainFrameTime advance = get_main_timer_sync().advance(frame_slice, physics_fps);
		double step = advance.idle_step;
		double scaled_step = step * time_scale;

		Engine::get_singleton()->_frame_step = step;
		Engine::get_singleton()->_physics_interpolation_fraction = advance.interpolation_fraction;

		Main::frame += ticks_elapsed;

		Main::last_ticks = ticks;

		static const int max_physics_steps = 8;
		if (get_fixed_fps() == -1 && advance.physics_steps > max_physics_steps) {
			step -= (advance.physics_steps - max_physics_steps) * frame_slice;
			advance.physics_steps = max_physics_steps;
		}

		static_physics_steps = advance.physics_steps;
		static_frame_slice = frame_slice;
		static_step = step;
		static_time_scale = time_scale;
		static_scaled_step = scaled_step;
		static_ticks = ticks;
	}

	_declspec(dllexport) bool updatePhysics(int physics_steps, float p_time) {
		bool exit = false;
		Engine::get_singleton()->_in_physics = true;
		uint64_t physics_process_ticks = 0;

		for (int iters = 0; iters < physics_steps; ++iters) {

			uint64_t physics_begin = OS::get_singleton()->get_ticks_usec();

			PhysicsServer::get_singleton()->sync();
			PhysicsServer::get_singleton()->flush_queries();

			if (OS::get_singleton()->get_main_loop()->iteration(p_time)) {
				exit = true;
				break;
			}

			get_message_queue()->flush();

			PhysicsServer::get_singleton()->step(p_time);

			get_message_queue()->flush();

			physics_process_ticks = MAX(physics_process_ticks, OS::get_singleton()->get_ticks_usec() - physics_begin); // keep the largest one for reference
			set_physics_process_max(MAX(OS::get_singleton()->get_ticks_usec() - physics_begin, get_physics_process_max()));
			Engine::get_singleton()->_physics_frames++;
		}

		Engine::get_singleton()->_in_physics = false;
		return exit;
	}

	_declspec(dllexport) bool postUpdateMisc(uint64_t idle_begin, uint64_t ticks) {
		bool exit = false;
		uint64_t idle_process_ticks = OS::get_singleton()->get_ticks_usec() - idle_begin;
		set_idle_process_max(MAX(idle_process_ticks, get_idle_process_max()));
		uint64_t frame_time = OS::get_singleton()->get_ticks_usec() - ticks;

		for (int i = 0; i < ScriptServer::get_language_count(); i++) {
			ScriptServer::get_language(i)->frame();
		}

		AudioServer::get_singleton()->update();

		Main::frames++;
		Engine::get_singleton()->_idle_frames++;

		if (Main::frame > 1000000) {

			//if (editor || project_manager) {
			//	if (print_fps) {
			//		print_line("Editor FPS: " + itos(Main::frames));
			//	}
			//}
			//else if (GLOBAL_GET("debug/settings/stdout/print_fps") || print_fps) {
			//	print_line("Game FPS: " + itos(Main::frames));
			//}

			Engine::get_singleton()->_fps = Main::frames;
			//performance->set_process_time(USEC_TO_SEC(idle_process_max));
			//performance->set_physics_process_time(USEC_TO_SEC(physics_process_max));
			set_idle_process_max(0);
			set_physics_process_max(0);

			Main::frame %= 1000000;
			Main::frames = 0;
		}

		Main::iterating--;

		if (get_fixed_fps() != -1)
			return exit;

		OS::get_singleton()->add_frame_delay(OS::get_singleton()->can_draw());

		return exit;
	}

	_declspec(dllexport) void updateDisplay(double scaled_step) {
		get_message_queue()->flush();
		VisualServer::get_singleton()->sync(); //sync if still drawing from previous frames.

		if (OS::get_singleton()->can_draw() && VisualServer::get_singleton()->is_render_loop_enabled()) {

			if ((!Main::force_redraw_requested) && OS::get_singleton()->is_in_low_processor_usage_mode()) {
				if (VisualServer::get_singleton()->has_changed()) {
					VisualServer::get_singleton()->draw(true, scaled_step); // flush visual commands
					Engine::get_singleton()->frames_drawn++;
				}
			}
			else {
				VisualServer::get_singleton()->draw(true, scaled_step); // flush visual commands
				Engine::get_singleton()->frames_drawn++;
				Main::force_redraw_requested = false;
			}
		}
	}

	_declspec(dllexport) uint64_t getTicks() {
		return OS::get_singleton()->get_ticks_usec();
	}

	_declspec(dllexport) bool updateIdle() {
		bool exit = false;
		if (OS::get_singleton()->get_main_loop()->idle(static_step * static_time_scale)) {
			exit = true;
		}
		return exit;
	}

	_declspec(dllexport) bool updatePhysicsStatic() {
		return updatePhysics(static_physics_steps, static_frame_slice * static_time_scale);
	}

	_declspec(dllexport) void updateDisplayStatic() {
		updateDisplay(static_scaled_step);
	}

	_declspec(dllexport) bool postUpdateMiscStatic(uint64_t idle_begin) {
		return postUpdateMisc(idle_begin, static_ticks);
	}

	_declspec(dllexport) bool updatingTemp() {
		updateTiming();
		bool exit = updatePhysics(static_physics_steps, static_frame_slice * static_time_scale);
		uint64_t idle_begin = getTicks();
		exit = updateIdle() || exit;
		updateDisplay(static_scaled_step);
		return exit || postUpdateMisc(idle_begin, static_ticks);
	}

}
