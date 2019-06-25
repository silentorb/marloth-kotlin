import os, sys, shutil
import json
import pathlib
import subprocess
import re


def task_xml(input, output):
    return '''    <Task>
        <Image value="" width="512" height="512" />
        <Selection value="" />
        <Filter value="{input}" />
        <Result path="{output}" />
        <Preset value="0" />
    </Task>'''.format(input=input, output=output)


def get_xml(input_dir, output_dir, names, transparency=False):
    extension = 'png' if transparency else 'jpg'
    tasks = [task_xml(input_dir + '/' + name + '.ffxml', output_dir + '/' + name + '.' + extension) for name in names]
    tasks_clause = '\n'.join(tasks)
    transparency_clause = 'true' if transparency else 'false'
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
            <RAMUsageLimit value="60" />
            <NormalMapFlipY value="false" />
        </RenderingOptions>
        <BitmapFormatOptions>
            <DefaultFormat value="JPG" />
            <JPG>
                <Quality value="98" />
                <FullPrecision value="true" />
            </JPG>
            <PNG>
                <BitDepth value="16" />
                <IncludeTransparency value="{transparency}" />
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
</Tasks>'''.format(tasks_clause=tasks_clause, transparency=transparency_clause)


module_dir = os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), '..'))


def prepare_path(dir):
    return os.path.join(module_dir, dir).replace('\\', '/')


def read_text_file(file_path):
    return pathlib.Path(file_path).read_text()


def write_text_file(file_path, content):
    with open(file_path, 'w') as stream:
        stream.write(content)


temp_path = prepare_path('build/textures')
xml_path = temp_path + '/textures.xml'
config = json.loads(read_text_file(prepare_path('config/config.json')))
# There are two SliderControl entries, the main one and the preset override.  The preset entry overrides
# the main entry so make sure we grab it (using the Controls clause.)
value_pattern = re.compile(r'Controls.*?SliderControl id.*?Value value="', re.DOTALL)


def render(input_dir, output_dir, names, transparency=False):
    xml_content = get_xml(input_dir, output_dir, names, transparency)
    write_text_file(xml_path, xml_content)
    subprocess.run([config['paths']['filter_forge'], xml_path])


def get_step_name(name, i):
    return name + '-' + str(i).zfill(3)


def get_anim_temp_file(step_name):
    return temp_path + '/' + step_name + '.ffxml'


def ensure_dir_exists(dir):
    if not os.path.exists(dir):
        os.mkdir(dir)


def prepare_animation(input_dir, name):
    input_file = input_dir + '/' + name + '.ffxml'
    content = read_text_file(input_file)
    first_match = value_pattern.search(content)
    if (first_match is None):
        raise Exception('Animated filter is missing time control')

    value_start = first_match.end()
    second_match = re.search(r'[\d.]+', content[value_start:])
    if (second_match is None):
        raise Exception('Invalid time control value')

    value_length = second_match.end()
    value_end = value_start + value_length

    step_max = 32
    step_range = range(0, step_max)
    for i in step_range:
        step_name = get_step_name(name, i)
        time_step = i / step_max
        new_content = content[:value_start] + str(time_step) + content[value_end:]
        write_text_file(get_anim_temp_file(step_name), new_content)

    return [get_step_name(name, i) for i in step_range]


def main():
    # Down the line we may clean out the build/textures folder if the quantity of temp files becomes significant
    # shutil.rmtree(temp_path)
    ensure_dir_exists(temp_path)
    input_dir = prepare_path('textures')
    output_dir = prepare_path('src/main/resources/textures')
    names = sys.argv[1:]
    singles = list(filter(lambda n: n[:5] != 'anim-', names))
    animated = list(filter(lambda n: n[:5] == 'anim-', names))
    render(input_dir, output_dir, singles)
    for animation in animated:
        anim_names = prepare_animation(input_dir, animation)
        render(temp_path, output_dir, anim_names, True)

    print('Exported', ', '.join(names))


if __name__ == '__main__':
    main()
