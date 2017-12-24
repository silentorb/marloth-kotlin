package sculpting

import sculpting.query.Companion.vertices
import spatial.*

class create {
  companion object {
/*
  auto half = size * 0.5f;
      New_Faces result;
      result.reserve(6);
      auto &top = square_up(mesh, {size.x, size.y}, half.z);
      auto &bottom = square_down(mesh, {size.x, size.y}, -half.z);

      result.push_back(&top);
      result.push_back(&bottom);

      auto top_vertices = query::vertices(top);
      auto initial_bottom_vertices = query::vertices(bottom);
      Base_Vertex *bottom_vertices[] = {
        initial_bottom_vertices[0],
        initial_bottom_vertices[3],
        initial_bottom_vertices[2],
        initial_bottom_vertices[1],
      };

      for (auto i = 0; i < 4; ++i) {
        auto a = i;
        auto b = i > 2 ? 0 : i + 1;

        Base_Vertex *v[] = {
          top_vertices[b], top_vertices[a],
          bottom_vertices[a], bottom_vertices[b]
        };
        result.push_back(&mesh.add_face(v, 4));
      }

      return result;
 */

    fun cube(mesh: HalfEdgeMesh, size: Vector3): Array<Face> {
      val half = size * 0.5f
      val top = squareUp(mesh, Vector2(size.x, size.y), half.z)
      val bottom = squareDown(mesh, Vector2(size.x, size.y), -half.z)

      val top_vertices = query.vertices(top)
      val initial_bottom_vertices = query.vertices(bottom)
      val bottom_vertices = arrayOf(
          initial_bottom_vertices[0],
          initial_bottom_vertices[3],
          initial_bottom_vertices[2],
          initial_bottom_vertices[1]
      )

      val sides = (0..3).map { a ->
        val b = if (a > 2) 0 else a + 1
        mesh.add_face(arrayOf(
            top_vertices[b], top_vertices[a],
            bottom_vertices[a], bottom_vertices[b]
        ))
      }
      return arrayOf<Face>(top, bottom)
          .plus(arrayOf())
    }

    fun squareDown(mesh: HalfEdgeMesh, size: Vector2, z: Float): Face {
      val half = size * 0.5f;
      return mesh.add_face(arrayOf(
          Vertex(Vector3(-half.x, -half.y, z)),
          Vertex(Vector3(-half.x, half.y, z)),
          Vertex(Vector3(half.x, half.y, z)),
          Vertex(Vector3(half.x, -half.y, z))
      ))
    }

    fun squareUp(mesh: HalfEdgeMesh, size: Vector2, z: Float): Face {
      val half = size * 0.5f;
      return mesh.add_face(arrayOf(
          Vertex(Vector3(-half.x, -half.y, z)),
          Vertex(Vector3(half.x, -half.y, z)),
          Vertex(Vector3(half.x, half.y, z)),
          Vertex(Vector3(-half.x, half.y, z))
      ))
    }

    fun flatTest(): HalfEdgeMesh {
      val mesh = HalfEdgeMesh()
      mesh.add_face(arrayOf(
          Vertex(Vector3(1f, 1f, 0f)),
          Vertex(Vector3(0.5f, 1f, 0f)),
          Vertex(Vector3(1f, 0.5f, 0f))
      ))

      mesh.add_face(arrayOf(
          Vertex(Vector3(-1f, -1f, 0f)),
          Vertex(Vector3(-1f, -0.5f, 0f)),
          Vertex(Vector3(-0.5f, -1f, 0f))
      ))
      return mesh
    }

  }
}