import bpy
mesh = bpy.context.object.data
vertices = [(1, 1, 1), (2, 2, 1), (1, 3, 3)]
indices = [tuple(range(0, 3))]
mesh.from_pydata(vertices, [], indices)
mesh.update(calc_edges=True)
