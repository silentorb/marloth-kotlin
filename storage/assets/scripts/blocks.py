import bpy
import os
import json
from os.path import dirname
from mathutils import Vector
from math import floor, ceil

cell_length = 10


def get_bounds(points):
    x = [v[0] for v in points]
    y = [v[1] for v in points]
    z = [v[2] for v in points]
    return [
        [min(x), min(y), min(z)],
        [max(x), max(y), max(z)]
    ]


def simplify_bounds(object):
    bound_box = object.bound_box
    transform = object.matrix_world
    points = [transform @ Vector(v) for v in bound_box]
    return get_bounds(points)


def prepare_object(object):
    return {
        'target': object.proxy.name,
        'location': object.location[:],
        'orientation': object.rotation_quaternion[:],
        'scale': object.scale[:]
    }


def convert_min_max(float_min_max):
    return [
        floor(float_min_max[0] / cell_length),
        ceil(float_min_max[1] / cell_length)
    ]


def has_overlap(bound_boxes, x, y, z):
    x1 = x * cell_length
    y1 = y * cell_length
    z1 = z * cell_length
    x2 = x1 + cell_length
    y2 = y1 + cell_length
    z2 = z1 + cell_length

    for b in bound_boxes:
        if b[0][0] < x2 and b[0][1] < y2 and b[0][2] < z2 and b[1][0] >= x1 and b[1][1] >= y1 and b[1][2] >= z1:
            return True

    # print(x, y, z)
    return False


def get_cells(bound_boxes):
    flattened = [point for points in bound_boxes for point in points]
    precise_total = get_bounds(flattened)
    mins = [floor(element / cell_length) for element in precise_total[0]]
    maxes = [ceil(element / cell_length) for element in precise_total[1]]
    result = []
    # print(mins)
    # print(maxes)
    for z in range(mins[2], maxes[2]):
        for y in range(mins[1], maxes[1]):
            for x in range(mins[0], maxes[0]):
                if has_overlap(bound_boxes, x, y, z):
                    result.append([x, y, z])
    return result


def prepare_annotation(annotation):
    return {
        'cell': [floor(element / cell_length) for element in annotation.location[:]],
        'attributes': annotation['cell-attributes'].split(',')
    }


def prepare_block(block):
    elements = [obj for obj in block.objects if obj.type != 'EMPTY']
    bound_boxes = [simplify_bounds(obj) for obj in elements]
    cells = get_cells(bound_boxes)
    # print(len(cells))
    annotations = [obj for obj in block.objects if obj.type == 'EMPTY']
    attributes = [prepare_annotation(obj) for obj in annotations]

    value = {
        'attributes': attributes,
        'cells': cells,
        'elements': list(map(prepare_object, elements))

    }
    return block.name, value


def export_blocks():
    input_blocks = bpy.data.collections["Blocks"].children
    output_blocks = dict(map(prepare_block, input_blocks))
    root = {
        'blocks': output_blocks
    }
    json_output = json.dumps(root)
    output_dir = os.path.join(dirname(dirname(dirname(bpy.data.filepath))), 'src', 'main', 'resources', 'blocks')
    output_file = os.path.join(output_dir, 'blocks.json')
    os.makedirs(output_dir, exist_ok=True)
    with open(output_file, "w") as text_file:
        text_file.write(json_output)
