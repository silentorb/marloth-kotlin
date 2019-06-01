import os.path
import subprocess
import json
import pathlib

config = json.loads(pathlib.Path('../config/config.json').read_text())

model_path = '../models'

def export_model(filename):
    filepath = os.path.abspath(os.path.join(model_path, filename))
    blender_path = config['paths']['blender_executable']
    subprocess.call([blender_path, filepath, '--background', '--python', 'export.py'])

# files = os.listdir(model_path)
# for file in files:
#     export_model(file)

export_model('person.blend')
