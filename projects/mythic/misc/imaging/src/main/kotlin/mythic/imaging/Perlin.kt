package mythic.imaging

import mythic.spatial.*
import org.joml.Vector2i
import org.joml.Vector3i
import org.joml.plus

private val permutation = intArrayOf(
    151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225,
    140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148,
    247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
    57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175,
    74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
    60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
    65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169,
    200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64,
    52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212,
    207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213,
    119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
    129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104,
    218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
    81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
    184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
    222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
)

private val p = IntArray(512) {
  if (it < 256) permutation[it] else permutation[it - 256]
}

private fun fade(t: Double) = t * t * t * (t * (t * 6 - 15) + 10)

private fun lerpOld(t: Double, a: Double, b: Double) = a + t * (b - a)

private fun grad(hash: Int, x: Double, y: Double, z: Double): Double {
  // Convert low 4 bits of hash code into 12 gradient directions
  val h = hash and 15
  val u = if (h < 8) x else y
  val v = if (h < 4) y else if (h == 12 || h == 14) x else z
  return (if ((h and 1) == 0) u else -u) +
      (if ((h and 2) == 0) v else -v)
}

fun noise(x: Double, y: Double, z: Double = 0.0): Double {
  // Find unit cube that contains point
  val xi = Math.floor(x).toInt() and 255
  val yi = Math.floor(y).toInt() and 255
  val zi = Math.floor(z).toInt() and 255

  // Find relative x, y, z of point in cube
  val xx = x - Math.floor(x)
  val yy = y - Math.floor(y)
  val zz = z - Math.floor(z)

  // Compute fade curves for each of xx, yy, zz
  val u = fade(xx)
  val v = fade(yy)
  val w = fade(zz)

  // Hash co-ordinates of the 8 cube corners
  // and add blended results from 8 corners of cube

  val a = p[xi] + yi
  val aa = p[a] + zi
  val ab = p[a + 1] + zi
  val b = p[xi + 1] + yi
  val ba = p[b] + zi
  val bb = p[b + 1] + zi

  return lerpOld(w, lerpOld(v, lerpOld(u, grad(p[aa], xx, yy, zz), grad(p[ba], xx - 1, yy, zz)),
      lerpOld(u, grad(p[ab], xx, yy - 1, zz), grad(p[bb], xx - 1, yy - 1, zz))),
      lerpOld(v, lerpOld(u, grad(p[aa + 1], xx, yy, zz - 1), grad(p[ba + 1], xx - 1, yy, zz - 1)),
          lerpOld(u, grad(p[ab + 1], xx, yy - 1, zz - 1), grad(p[bb + 1], xx - 1, yy - 1, zz - 1))))
}

fun fastFloor(f: Float): Int =
    if (f >= 0) f.toInt() else f.toInt() - 1

enum class Interp {
  Linear, Hermite, Quintic
}

private fun lerp(a: Float, b: Float, t: Float): Float {
  return a + t * (b - a)
}

private val GRAD_3D = arrayOf(Vector3(1f, 1f, 0f), Vector3(-1f, 1f, 0f),
    Vector3(1f, -1f, 0f), Vector3(-1f, -1f, 0f), Vector3(1f, 0f, 1f),
    Vector3(-1f, 0f, 1f), Vector3(1f, 0f, -1f), Vector3(-1f, 0f, -1f),
    Vector3(0f, 1f, 1f), Vector3(0f, -1f, 1f),
    Vector3(0f, 1f, -1f), Vector3(0f, -1f, -1f), Vector3(1f, 1f, 0f),
    Vector3(0f, -1f, 1f), Vector3(-1f, 1f, 0f), Vector3(0f, -1f, -1f))

private val X_PRIME = 1619
private val Y_PRIME = 31337
private val Z_PRIME = 6971
private val W_PRIME = 1013

private fun grad3d(seed: Int, x: Int, y: Int, z: Int, xd: Float, yd: Float, zd: Float): Float {
  var hash = seed
  hash = hash xor X_PRIME * x
  hash = hash xor Y_PRIME * y
  hash = hash xor Z_PRIME * z

  hash = hash * hash * hash * 60493
  hash = hash shr 13 xor hash

  val (x1, y1, z1) = GRAD_3D[hash and 15]

  return xd * x1 + yd * y1 + zd * z1
}

fun perlin3d(seed: Int, x: Float, y: Float, z: Float = 0f): Float {
  val input = Vector3(x, y, z)
  val a = Vector3i(fastFloor(x), fastFloor(y), fastFloor(z))
  val b = a + 1
  val s = input - a.toVector3()
  val d = s - 1f

  val xf00 = lerp(grad3d(seed, a.x, a.y, a.z, s.x, s.y, s.z), grad3d(seed, b.x, a.y, a.z, d.x, s.y, s.z), s.x)
  val xf10 = lerp(grad3d(seed, a.x, b.y, a.z, s.x, d.y, s.z), grad3d(seed, b.x, b.y, a.z, d.x, d.y, s.z), s.x)
  val xf01 = lerp(grad3d(seed, a.x, a.y, b.z, s.x, s.y, d.z), grad3d(seed, b.x, a.y, b.z, d.x, s.y, d.z), s.x)
  val xf11 = lerp(grad3d(seed, a.x, b.y, b.z, s.x, d.y, d.z), grad3d(seed, b.x, b.y, b.z, d.x, d.y, d.z), s.x)

  val yf0 = lerp(xf00, xf10, s.y)
  val yf1 = lerp(xf01, xf11, s.y)

  return lerp(yf0, yf1, s.z)
}

private val GRAD_2D = arrayOf(Vector2(-1f, -1f), Vector2(1f, -1f),
    Vector2(-1f, 1f), Vector2(1f, 1f), Vector2(0f, -1f),
    Vector2(-1f, 0f), Vector2(0f, 1f), Vector2(1f, 0f))
fun dotGridGradient(x: Float, y: Float): (Int, Int) -> Float = { ix, iy ->

  // Precomputed (or otherwise) gradient vectors at each grid node
  // Compute the distance vector
  val dx = x - ix.toFloat();
  val dy = y - iy.toFloat();

  // Compute the dot-product
  (dx * GRAD_2D[iy][ix][0] + dy * GRAD_2D[iy][ix][1]);
}

fun perlin2d(x: Float, y: Float): Float {
  val input = Vector2(x, y)
  val a = Vector2i(fastFloor(x), fastFloor(y))
  val b = a + 1
  val s = input - a.toVector2()

  val grid = dotGridGradient(x, y)

  val ix0 = lerp(grid(a.x, a.y), grid(b.x, a.y), s.x)
  val ix1 = lerp(grid(a.x, b.y), grid(b.x, b.y), s.x)

  return lerp(ix0, ix1, s.y)
}

/*
  // Determine grid cell coordinates
    int x0 = int(x);
    int x1 = x0 + 1;
    int y0 = int(y);
    int y1 = y0 + 1;

    // Determine interpolation weights
    // Could also use higher order polynomial/s-curve here
    float sx = x - (float)x0;
    float sy = y - (float)y0;

    // Interpolate between grid point gradients
    float n0, n1, ix0, ix1, value;
    n0 = dotGridGradient(x0, y0, x, y);
    n1 = dotGridGradient(x1, y0, x, y);
    ix0 = lerp(n0, n1, sx);
    n0 = dotGridGradient(x0, y1, x, y);
    n1 = dotGridGradient(x1, y1, x, y);
    ix1 = lerp(n0, n1, sx);
    value = lerp(ix0, ix1, sy);

    return value;
 */