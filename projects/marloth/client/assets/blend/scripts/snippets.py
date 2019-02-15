
# Get the currently selected bones while in edit mode
def ebones():
    return bpy.context.selected_editable_bones

# Copy bone positions from the first selected bone to the second selected bone
def cp():
    ebones()[0].head = reverse(ebones()[1].head)
    ebones()[0].tail = reverse(ebones()[1].tail)