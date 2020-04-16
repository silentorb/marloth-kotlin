import os.path
import subprocess
import json
import pathlib
import sys

config = json.loads(pathlib.Path('../config/config.json').read_text())

model_path = '../models'
sub_dirs = [ 'architecture', 'objects']


def export_model(dir, filename):
    filepath = os.path.abspath(os.path.join(dir, filename))
    blender_path = config['paths']['blender']
    subprocess.call([blender_path, filepath, '--background', '--python', 'export.py'])


if len(sys.argv) > 1:
    export_model(model_path, sys.argv[1] + '.blend')
else:
    for dir in sub_dirs:
        full_dir = os.path.join(model_path, dir)
        files = os.listdir(full_dir)
        for file in files:
            export_model(full_dir, file)
