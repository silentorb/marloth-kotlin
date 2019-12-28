import sys
import subprocess
import re

from utility import prepare_path, ensure_dir_exists, write_text_file, load_config


input_dir = prepare_path('audio/faust')
output_dir = prepare_path('src/main/java/marloth/assets/audio/sounds')

config = load_config()


def prepare_java_code(code, class_name):
    new_code = code.replace('mydsp', class_name) \
        .replace(' extends dsp', ' extends marloth.assets.audio.utility.SoundGenerator')

    c2 = re.compile(r'public void metadata.*?}\s*', re.DOTALL).sub('', new_code)
    c3 = re.compile(r'public void buildUserInterface.*?}\s*', re.DOTALL).sub('', c2)
    return "package marloth.assets.audio;\n\n" + c3

def render(name):
    class_name = name.capitalize()
    dsp_file_path = input_dir + '/' + name + '.dsp'
    proc = subprocess.run([config['paths']['faust'], '-lang', 'java', dsp_file_path], stdout=subprocess.PIPE, universal_newlines =True)
    # print(proc.stdout)
    content = proc.stdout#.decode("utf-8")
    code = prepare_java_code(content, class_name)
    # code = content
    write_text_file(output_dir + '/' + class_name + '.java', code)


def main():
    ensure_dir_exists(output_dir)
    names = sys.argv[1:]
    for name in names:
        render(name)
    print('Exported', ', '.join(names))


if __name__ == '__main__':
    main()
