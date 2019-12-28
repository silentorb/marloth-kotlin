import sys
import subprocess

from utility import prepare_path, ensure_dir_exists, write_text_file, load_config


def template(synth_file_path, temp_file_path):
    return f'''(
var server = Server(\\nrt,
    options: ServerOptions.new
    .numOutputBusChannels_(1)
    .numInputBusChannels_(1)
);

"{synth_file_path}".load;

a.recordNRT(
    outputFilePath: "{temp_file_path}".standardizePath,
    headerFormat: "wav",
    sampleFormat: "int16",
    options: server.options,
    duration: 1,
    action: {{ 0.exit }}
);

server.remove;
)
'''

temp_path = prepare_path('build/audio')
resource_path = prepare_path('scripts/resources')
input_dir = prepare_path('audio/supercollider')

config = load_config()


def render(name):
    synth_file_path = input_dir + '/' + name + '.scd'
    content = template(synth_file_path, temp_path + '/temp.wav')
    script_path = temp_path + '/temp.scd'
    write_text_file(script_path, content)
    subprocess.run([config['paths']['supercollider'], script_path])


def main():
    ensure_dir_exists(temp_path)
    input_dir = prepare_path('audio')
    output_dir = prepare_path('src/main/resources/audio')
    names = sys.argv[1:]
    for name in names:
        render(name)
    print('Exported', ', '.join(names))


if __name__ == '__main__':
    main()
