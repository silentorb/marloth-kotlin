(
var duration = 3;

var click = SynthDef("click", { |out|
	var amp = EnvGen.kr(
        Env(
            levels: [0, 0.4, 0],
            times: [0.009, 0.1],
      curve: [-1, -10]
        ),
    doneAction: Done.freeSelf
    );
  var noise = WhiteNoise.ar();
  var freq = Line.kr(2000, 1000, 0.007);
  var highNoise = BPF.ar(noise, freq, Line.kr(0.3, 0.1, 0.03)) * 3;
  var bip = SinOsc.ar(freq) * 0.2;
  var highBip = SinOsc.ar(1500) * 0.1;
  var output = (highBip + bip + highNoise * SinOsc.ar(freq, 0.5)) * amp;
  Out.ar(out, output)
}).add;

var shoot = SynthDef("shoot", { |out|
	var ampEnv = EnvGen.kr(
        Env(
            levels: [0, 1, 0],
            times: [0.01, 1.5],
      curve: [-2, -10]
        ),
    doneAction: Done.freeSelf
    );
	var osc = WhiteNoise.ar();
  var high = BPF.ar(osc, Line.kr(100, 0, 0.04, 1, 300), Line.kr(0.4, 0.07, 0.05)) * 8;
  var low = SinOsc.ar(70) * Line.kr(1, 0.1, 0.0);
  var output = (high + low) * ampEnv;
    Out.ar(out, output)
}).add;

var echo = SynthDef("echo", { |out|
	var ampEnv = EnvGen.kr(
        Env(
            levels: [0, 0.35, 0],
            times: [0.03, 0.8],
      curve: [-1, -10]
        ),
    doneAction: Done.freeSelf
    );
	var osc = WhiteNoise.ar();
	var filter = BPF.ar(osc, 800, 0.25);
	var output = filter * ampEnv;
    Out.ar(out, output)
}).add;

Score.play(
	[
		[0, [\s_new, \click, -1, 0, 0]],
    [0.05, [\s_new, \shoot, -1, 0, 0]],
     [0.15, [\s_new, \echo, -1, 0, 0]],
    [duration, [\c_set, 0, 0]] // finish
	]
);
)
