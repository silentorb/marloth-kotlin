import os, sys
import json
import pathlib
import subprocess

def task_xml(input, output):
    return '''    <Task>
        <Image value="" width="1024" height="1024" />
        <Selection value="" />
        <Filter value="{input}" />
        <Result path="{output}" />
        <Preset value="0" />
    </Task>'''.format(input = input, output = output)


def get_xml(input_dir, output_dir, names):
    tasks = [task_xml(input_dir + '/' + name + '.ffxml', output_dir + '/' + name + '.jpg') for name in names]
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
                <IncludeTransparency value="false" />
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
</Tasks>'''.format(tasks_clause = tasks_clause)

module_dir = os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), '..'))

def prepare_path(dir):
    return os.path.join(module_dir, dir).replace('\\', '/')


if __name__ == '__main__':
    xml_path = prepare_path('build/textures.xml')
    input_dir = prepare_path('textures')
    output_dir = prepare_path('src/main/resources/textures')
    names = sys.argv[1:]
    config = json.loads(pathlib.Path(prepare_path('config/config.json')).read_text())

    xml_content = get_xml(input_dir, output_dir, names)
    with open(xml_path, "w") as stream:
        stream.write(xml_content)
    subprocess.run([config['paths']['filter_forge'], xml_path])
    print('Exported', ', '.join(names))
