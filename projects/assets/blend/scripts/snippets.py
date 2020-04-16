
# Get the currently selected bones while in edit mode
def ebones():
    return bpy.context.selected_editable_bones

# Copy bone positions from the first selected bone to the second selected bone
def cp():
    ebones()[0].head = reverse(ebones()[1].head)
    ebones()[0].tail = reverse(ebones()[1].tail)

import bpy

def cleanupMirroredWeights(obj):
    for v in obj.data.vertices:
        flagged = []
        vIsLeft = v.co.x > 0
        vIsRight = v.co.x < 0

        for g in v.groups:
            group = obj.vertex_groups[g.group]
            #print(group.name)
            gIsLeft = '.L' in group.name
            gIsRight = '.R' in group.name
            if (vIsLeft and gIsRight) or (vIsRight and gIsLeft):
                flagged.insert(0, group)

        for f in flagged:
            f.remove([v.index])

# cleanupMirroredWeights(bpy.context.active_object)