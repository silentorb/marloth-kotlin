import sys
import subprocess
import re
import os

from utility import prepare_path, ensure_dir_exists, write_text_file, load_config, read_text_file


def template(synth, temp_file_path):
    return f'''(
var server = Server(\\nrt,
    options: ServerOptions.new
    .numOutputBusChannels_(1)
    .numInputBusChannels_(1)
);

{synth}

score.recordNRT(
    outputFilePath: "{temp_file_path}".standardizePath,
    headerFormat: "wav",
    sampleFormat: "int16",
    options: server.options,
    duration: duration,
    action: {{ 0.exit }}
);

server.remove;
)
'''

temp_path = prepare_path('build/audio')
input_dir = prepare_path('audio/supercollider/sounds')
output_dir = prepare_path('src/main/resources/audio')
temp_wav_path = temp_path + '/temp.wav'

config = load_config()


def process_synth_code(code):
    a = code.replace('Score.play', 'var score = Score')
    b = re.compile(r'^\s*\(\s*', re.DOTALL).sub('', a)
    c = re.compile(r'\s*\)\s*$', re.DOTALL).sub('', b)
    synths = re.findall(r'SynthDef\("(\w+)"', c)
    synths_clause = '\n'.join([f"[0.0, ['/d_recv', {synth}.asBytes]]," for synth in synths])
    d = re.compile(r'Score\(\s*\[').sub('Score([\n' + synths_clause, c)
    return d


def encode_ogg_vorbis(name):
    audio_file_path = output_dir + '/' + name + '.ogg'
    subprocess.run([config['paths']['oggenc'], '-o', audio_file_path, temp_wav_path])


def render(name):
    synth_file_path = input_dir + '/' + name + '.scd'
    synth = process_synth_code(read_text_file(synth_file_path))
    content = template(synth, temp_wav_path)
    script_path = temp_path + '/temp.scd'
    write_text_file(script_path, content)
    sclang_path = config['paths']['sclang']
    args = [sclang_path, '-d', os.path.dirname(sclang_path), script_path]
    print(" ".join(args))
    pipes = subprocess.run(args, timeout=3000, text=True, capture_output=True)
    print(pipes.stdout)
    # Can't rely on the return code or stderr
    if 'ERROR' in pipes.stdout:
        exit(1)

    encode_ogg_vorbis(name)


def main():
    ensure_dir_exists(temp_path)
    names = sys.argv[1:]
    for name in names:
        render(name)
    print('Exported', ', '.join(names))


if __name__ == '__main__':
    main()
