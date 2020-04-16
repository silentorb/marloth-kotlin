(
var duration = 3;

var grunt = SynthDef("grunt", { |out, freq=100, duration = 0.25|
	var modEnv = EnvGen.kr(
        Env(
            levels: [1200, 1800, 1400],
            times: [0, 0.1, duration],
            curve: 8
        ),
        gate: Impulse.kr(3)
    );
	var freq2 = freq + Saw.ar(freq - 10, 1000);
	var chain = {|list| list.reduce({ |a, b| b(a) }) };

	var	mod = Saw.ar(
		freq,
		200,
		400
	);
  var attack = 0.01;
  var filterAttack = 0.05;
	var filterEnv = EnvGen.kr(
        Env(
            levels: [0.2, 1, 0.2],
            times: [filterAttack, duration - filterAttack],
            curve: 8
        )
    );

	var ampEnv = EnvGen.kr(
        Env(
            levels: [0, 1, 0],
            times: [attack, duration - attack],
      curve: [-1, \sine]
        ),
    doneAction: Done.freeSelf
    );
	var osc = Pulse.ar(freq2, 0.5) * ampEnv;
	var lowFilter = LPF.ar(osc, 1200 * filterEnv);
	var output = lowFilter;
    Out.ar(out, output)
}).add;

Score.play(
	[
		[0, [\s_new, \grunt, -1, 0, 0, \freq, 100]],
    [0.2, [\s_new, \grunt, -1, 0, 0, \freq, 200]],
    [1, [\s_new, \grunt, -1, 0, 0, \freq, 50]],
    [1.9, [\s_new, \grunt, -1, 0, 0, \freq, 120]],
		[duration, [\c_set, 0, 0]] // finish
	]
);
)
