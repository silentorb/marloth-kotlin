import os, sys
import json
import pathlib
import subprocess

# Filter forge source files can optionally embed meta data into the file name.
# Meta data could be added inside the file but would increase processing
# Since in most cases the python code never actually parses the file contents.

# The format for the meta data is a period followed by a two digit number representing the number of iterations,
# and optionally the letter 'a' to rasterize a texture with an alpha channel.

# Filter Forge's CLI has a bug that prevents generating transparent textures.  Only filters support transparency,
# so when rasterizing a transparent texture, an empty source image with an alpha channel must be referenced.
# Filter Forge has no resizing so the soruce image dimensions define the output dimensions.

default_length = 512
module_dir = os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), '..'))


def prepare_path(dir):
    return os.path.join(module_dir, dir).replace('\\', '/')


def read_text_file(file_path):
    return pathlib.Path(file_path).read_text()


temp_path = prepare_path('build/textures')
resource_path = prepare_path('scripts/resources')
xml_path = temp_path + '/textures.xml'
config = json.loads(read_text_file(prepare_path('config/config.json')))


def get_source_image_clause(width, height, format):
    if format != 'PNG':
        return ''
    else:
        return '{resource_path}/{width}x{height}.png'.format(width=width, height=height, resource_path=resource_path)


def task_xml(input, output, width, height, format):
    source_path = get_source_image_clause(width, height, format)
    return '''    <Task>
        <Image value="{source_path}" width="{width}" height="{height}" />
        <Selection value="" />
        <Filter value="{input}" />
        <Result path="{output}" format="{format}" />
        <Preset value="0" />
    </Task>'''.format(input=input, output=output, width=width, height=height, format=format, source_path=source_path)


def name_to_task(input_dir, output_dir, name):
    transparency = False
    tokens = name.split('.')
    width = default_length
    height = default_length
    output_name = name
    if len(tokens) > 1:
        meta = tokens[1]
        if 'a' in meta[2]:
            transparency = True
            output_name = output_name[:-1]
        width = width * int(meta[:2])
    extension = 'png' if transparency else 'jpg'
    input_file = input_dir + '/' + name + '.ffxml'
    output_file = output_dir + '/' + output_name + '.' + extension
    return task_xml(input_file, output_file, width, height, extension.upper())

def get_xml(input_dir, output_dir, names):
    tasks = [name_to_task(input_dir, output_dir, name) for name in names]
    tasks_clause = '\n'.join(tasks)
    return '''<?xml version="1.0" encoding="utf-8" ?>
<Tasks>
{tasks_clause}    
    <GlobalSettings>
        <RenderingOptions>
            <UseMultithreading value="true" />
            <Dither value="true" />
            <Progressive value="true" />
            <OptimizeBlurs value="true" />
            <AntiAliasBitmapComponentSources value="false" />
            <Jitter value="0" />
            <TemporaryFilesLocation value="" />
            <RAMUsageLimit value="90" />
            <NormalMapFlipY value="false" />
        </RenderingOptions>
        <BitmapFormatOptions>
            <DefaultFormat value="JPG" />
            <JPG>
                <Quality value="98" />
                <FullPrecision value="true" />
            </JPG>
            <PNG>
                <BitDepth value="8" />
                <IncludeTransparency value="true" />
                <ImageCompression value="BEST" />
            </PNG>
            <BMP>
                <BitDepth value="32" />
                <FlipRowOrder value="false" />
            </BMP>
            <TIF>
                <BitDepth value="8" />
                <FloatFormat value="false" />
                <IncludeTransparency value="true" />
                <ImageCompression value="LZW" />
            </TIF>
            <TGA>
                <BitDepth value="32" />
                <IncludeTransparency value="true" />
                <FlipRowOrder value="false" />
            </TGA>
            <EXR>
                <BitDepth value="32" />
                <IncludeTransparency value="true" />
                <ImageCompression value="PIZ" />
                <FlipRowOrder value="false" />
            </EXR>
            <PFM />
        </BitmapFormatOptions>
    </GlobalSettings>
</Tasks>'''.format(tasks_clause=tasks_clause)


def write_text_file(file_path, content):
    with open(file_path, 'w') as stream:
        stream.write(content)


def render(input_dir, output_dir, names):
    xml_content = get_xml(input_dir, output_dir, names)
    write_text_file(xml_path, xml_content)
    subprocess.run([config['paths']['filter_forge'], xml_path])


def get_step_name(name, i):
    return name + '-' + str(i).zfill(3)


def get_anim_temp_file(step_name):
    return temp_path + '/' + step_name + '.ffxml'


def ensure_dir_exists(dir):
    if not os.path.exists(dir):
        os.mkdir(dir)


def main():
    ensure_dir_exists(temp_path)
    input_dir = prepare_path('textures')
    output_dir = prepare_path('src/main/resources/textures')
    names = sys.argv[1:]
    render(input_dir, output_dir, names)
    print('Exported', ', '.join(names))


if __name__ == '__main__':
    main()
