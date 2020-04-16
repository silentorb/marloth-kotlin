import os, sys
import pathlib
import json

module_dir = os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), '..'))


def prepare_path(dir):
    return os.path.join(module_dir, dir).replace('\\', '/')


def write_text_file(file_path, content):
    with open(file_path, 'w') as stream:
        stream.write(content)


def ensure_dir_exists(dir):
    if not os.path.exists(dir):
        ensure_dir_exists(pathlib.Path(dir).parent)
        os.mkdir(dir)


def read_text_file(file_path):
    return pathlib.Path(file_path).read_text()


def load_config():
    return json.loads(read_text_file(prepare_path('config/config.json')))
