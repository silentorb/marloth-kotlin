var score = Score([
    [0.0, ['/d_recv',
        SynthDef(\NRTsine, { |out, freq = 440|
            Out.ar(out, SinOsc.ar(freq, 0, 0.2).dup)
        }).asBytes
    ]],
    [0.0, (x = Synth.basicNew(\NRTsine, server, 1000)).newMsg(args: [freq: 400])],
    [1.0, x.freeMsg]
]);
