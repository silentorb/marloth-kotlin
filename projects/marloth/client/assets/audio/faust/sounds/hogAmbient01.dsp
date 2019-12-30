import("stdfaust.lib");
range(low, high, unit) = unit * (high - low) + low;
ctFreq = os.lf_triangle(10) + 1 / 2 * 500;
q = 5;
gain = 1;
//process = no.noise : fi.resonlp(ctFreq,q,gain);
gain2 = os.lf_saw(2) / 2 + 0.5;
process = os.triangle(500 * range(0.5, 1, gain2));
