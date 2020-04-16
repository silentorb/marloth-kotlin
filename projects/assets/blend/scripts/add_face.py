import bpy

from itertools import chain, islice, accumulate


def add_face(mesh, vertices, faces):
    vertex_start = len(mesh.vertices)
    polygon_start = len(mesh.polygons)

    face_lengths = tuple(map(len, faces))
    mesh.vertices.add(len(vertices))
    mesh.loops.add(sum(face_lengths))
    mesh.polygons.add(len(faces))

    for i in range(0, len(vertices)):
        mesh.vertices[i + vertex_start].co = vertices[i]

    adjusted_faces = [f for f in faces]
    indices = tuple(chain.from_iterable(adjusted_faces))
    loop_starts = tuple(islice(chain([0], accumulate(face_lengths)), len(faces)))

    for i in range(0, len(faces)):
        face = mesh.polygons[i + polygon_start]
        face.loop_total = face_lengths
        face.loop_start = loop_starts
        face.vertices = indices

    mesh.update(calc_edges=True)


mesh = bpy.context.object.data
vertices = [(1, 1, 1), (2, 2, 1), (1, 3, 3)]
faces = [tuple(range(0, 3))]
add_face(mesh, vertices, faces)
