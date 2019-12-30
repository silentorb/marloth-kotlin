#if _WIN32
#include <windows.h>
#endif

#include "mydsp.c"
#include "write_wav.c"

int main(int argc, char *argv[]) {
    if (argc < 2) return -1;

    const char *outputPath = argv[1];

    // Define some variables for the sound
    float sampleRate = 44100.0; // hertz
    float freq = 440.0;         // hertz
    float duration = 0.5;       // seconds

    int nSamples = (int)(duration*sampleRate);

    // Create a mono (1), 32-bit sound and set the duration
    Wave mySound = makeWave((int)sampleRate,1,32);
    waveSetDuration( &mySound, duration );
    mydsp *dsp = newmydsp();
    initmydsp(dsp, sampleRate);
    instanceClearmydsp(dsp);

    // Add all of the data
    int i;
    float frameData[1];
    float* f = frameData;
    for(i=0; i<nSamples; i+=1 ){
        computemydsp(dsp, 1, NULL, &f);
        waveAddSample( &mySound, frameData );
    }

    deletemydsp(dsp);

    // Write it to a file and clean up when done
    waveToFile( &mySound, outputPath);
    waveDestroy( &mySound );

    return 0;
}
