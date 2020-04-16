import sys
import subprocess
import re
from shutil import copy

from utility import prepare_path, ensure_dir_exists, write_text_file, load_config

input_dir = prepare_path('audio/faust/sounds')
output_dir = prepare_path('src/main/resources/audio')
temp_path = prepare_path('build/audio')
resource_path = prepare_path('scripts/resources/wav')
temp_wav_path = temp_path + '/temp.wav'

config = load_config()


def prepare_cpp_code(code):
    new_code = code.replace(' : public dsp', '')
    c2 = re.compile(r'void metadata.*?}\s*', re.DOTALL).sub('', new_code)
    return re.compile(r'void buildUserInterface.*?}\s*', re.DOTALL).sub('', c2)


def render_code(name):
    dsp_file_path = input_dir + '/' + name + '.dsp'
    proc = subprocess.run([config['paths']['faust'], '-lang', 'c', dsp_file_path], stdout=subprocess.PIPE, universal_newlines =True)
    code = prepare_cpp_code(proc.stdout)
    write_text_file(temp_path + '/mydsp.c', code)


def render_wav():
    subprocess.run([config['paths']['gcc'], '-static', '-o', 'temp.exe', temp_path + '/main.c'], cwd=temp_path)
    subprocess.run([temp_path + '/temp.exe', temp_wav_path ])


def encode_ogg_vorbis(name):
    audio_file_path = output_dir + '/' + name + '.ogg'
    subprocess.run([config['paths']['oggenc'], '-o', audio_file_path, temp_wav_path])


def render(name):
    render_code(name)
    render_wav()
    encode_ogg_vorbis(name)


def main():
    ensure_dir_exists(output_dir)
    ensure_dir_exists(temp_path)
    copy(resource_path + '/main.c', temp_path)
    copy(resource_path + '/write_wav.c', temp_path)
    names = sys.argv[1:]
    for name in names:
        render(name)
    print('Exported', ', '.join(names))


if __name__ == '__main__':
    main()
