package marloth.texture_generation

import mythic.spatial.*
import org.joml.*

private val STRETCH_CONSTANT_2D = -0.211324865405187f   //(1/Math.sqrt(2+1)-1)/2;
private val SQUISH_CONSTANT_2D = 0.366025403784439f      //(Math.sqrt(2+1)-1)/2;
private val STRETCH_CONSTANT_3D = -1.0f / 6              //(1/Math.sqrt(3+1)-1)/3;
private val SQUISH_CONSTANT_3D = 1.0f / 3                //(Math.sqrt(3+1)-1)/3;
private val STRETCH_CONSTANT_4D = -0.138196601125011f    //(1/Math.sqrt(4+1)-1)/4;
private val SQUISH_CONSTANT_4D = 0.309016994374947f      //(Math.sqrt(4+1)-1)/4;

private val NORM_CONSTANT_2D = 47.0f
private val NORM_CONSTANT_3D = 103.0f
private val NORM_CONSTANT_4D = 30.0f

private val DEFAULT_SEED: Long = 0

class OpenSimplexNoiseKotlin {

  private var perm: ShortArray
  private var permGradIndex3D: ShortArray

  constructor(perm: ShortArray) {
    this.perm = perm
    permGradIndex3D = ShortArray(256)

    for (i in 0..255) {
      //Since 3D has 24 gradients, simple bitmask won't work, so precompute modulo array.
      permGradIndex3D[i] = (perm[i] % (gradients3D.size / 3) * 3) as Short
    }
  }

  //Initializes the class using a permutation array generated from a 64-bit seed.
  //Generates a proper permutation (i.e. doesn't merely perform N successive pair swaps on a base array)
  //Uses a simple 64-bit LCG.
  @JvmOverloads constructor(seed: Long = DEFAULT_SEED) {
    var seed = seed
    perm = ShortArray(256)
    permGradIndex3D = ShortArray(256)
    val source = ShortArray(256)
    for (i in 0..255)
      source[i] = i.toShort()
    seed = seed * 6364136223846793005L + 1442695040888963407L
    seed = seed * 6364136223846793005L + 1442695040888963407L
    seed = seed * 6364136223846793005L + 1442695040888963407L
    for (i in 255 downTo 0) {
      seed = seed * 6364136223846793005L + 1442695040888963407L
      var r = ((seed + 31) % (i + 1)).toInt()
      if (r < 0)
        r += i + 1
      perm[i] = source[r]
      permGradIndex3D[i] = (perm[i] % (gradients3D.size / 3) * 3).toShort()
      source[r] = source[i]
    }
  }

  fun cool(ins: Vector2, sb: Vector2, d0: Vector2): Vector4 {
    val inSum = ins.x + ins.y

    val offset2 = if (inSum <= 1) {
      //We're inside the triangle (2-Simplex) at (0,0)
      val insz = 1 - inSum
      val offset = Vector4(1f, -1f, -1f, 1f)
      if (insz > ins.x || insz > ins.y) { //(0,0) is one of the closest two triangular vertices
        if (ins.x > ins.y) offset else -offset
      } else { //(1,0) and (0,1) are the closest two vertices.
        Vector4(1f, 1f, -1.0f - 2 * SQUISH_CONSTANT_2D, -1.0f - 2 * SQUISH_CONSTANT_2D)
      }
    } else {
      //We're inside the triangle (2-Simplex) at (1,1)
      val insz = 2 - inSum
      if (insz < ins.x || insz < ins.y) { //(0,0) is one of the closest two triangular vertices
        val offset = if (ins.x > ins.y)
          Vector4(2f, 0f, -2f - 2f * SQUISH_CONSTANT_2D, -2f * SQUISH_CONSTANT_2D)
        else
          Vector4(0f, 2f, -2f * SQUISH_CONSTANT_2D, -2.0f - 2f * SQUISH_CONSTANT_2D)
        if (ins.x > ins.y) offset else -offset
      } else { //(1,0) and (0,1) are the closest two vertices.
        Vector4()
      }
    }

    return Vector4(sb.x, sb.y, d0.x, d0.y) + offset2
  }

  fun wow(d: Vector2, sb: Vector2): Float {
    val attn1 = 2.0f - d.x * d.x - d.y * d.y
    return if (attn1 > 0) {
      attn1 * attn1 * attn1 * attn1 * extrapolate(sb.x.toInt(), sb.y.toInt(), d.x, d.y)
    } else
      0f
  }

  fun eval(x: Float, y: Float): Float {
    val input = Vector2(x, y)
    //Place input coordinates onto grid.
    val stretchOffset = (x + y) * STRETCH_CONSTANT_2D
    val s = input + stretchOffset

    //Floor to get grid coordinates of rhombus (stretched square) super-cell origin.
    val sb = Vector2(fastFloor(s.x), fastFloor(s.y))

    //Skew out to get actual coordinates of rhombus origin.
    val squishOffset = (sb.x + sb.y) * SQUISH_CONSTANT_2D

    //Compute grid coordinates relative to rhombus origin.
    val ins = s - sb

    //Sum those together to get a value that determines which region we're in.
    val inSum = ins.x + ins.y

    //Positions relative to origin point.
    val d0 = input - (sb + squishOffset)

    //We'll be defining these inside the next block and using them afterwards.
    val db = d0 - SQUISH_CONSTANT_2D

    //Contribution (1,0)
    val d1 = db + Vector2(-1.0f, 0f)

    //Contribution (0,1)
    val d2 = db + Vector2(0f, -1.0f)

    val foo = cool(ins, sb, d0)

    val (sb2, d02) = if (inSum > 1)
      Pair(sb + 1f, d0 - 1.0f - 2 * SQUISH_CONSTANT_2D)
    else
      Pair(sb, d0)

    val a1 = wow(d1, sb + Vector2(1f, 0f))
    val a2 = wow(d2, sb + Vector2(0f, 1f))
    val a3 = wow(d02, sb2)
    val a4 = wow(foo.zw, foo.xy())

    val value = a1 + a2 + a3 + a4

    return value / NORM_CONSTANT_2D
  }

  /*
    fun eval(x: Float, y: Float, z: Float, w: Float): Float {
      val input = Vector4(x, y, z, w)
      //Place input coordinates on simplectic honeycomb.
      val stretchOffset = (x + y + z + w) * STRETCH_CONSTANT_4D
      val s = input + stretchOffset

      //Floor to get simplectic honeycomb coordinates of rhombo-hypercube super-cell origin.
      val sb = Vector4(fastFloor(s.x), fastFloor(s.y), fastFloor(s.z), fastFloor(s.w))

      //Skew out to get actual coordinates of stretched rhombo-hypercube origin. We'll need these later.
      val squishOffset = (sb.x + sb.y + sb.z + sb.w) * SQUISH_CONSTANT_4D
      val b = s + squishOffset

      //Compute simplectic honeycomb coordinates relative to rhombo-hypercube origin.
      val ins = s - sb

      //Sum those together to get a value that determines which region we're in.
      val inSum = ins.x + ins.y + ins.z + ins.w

      //Positions relative to origin point.
      val d0 = input - b

      //We'll be defining these inside the next block and using them afterwards.
      val d_ext0 = Vector4(0f)
      val d_ext1 = Vector4(0f)
      var d_ext2 = Vector4(0f)

      var sv_ext0 = Vector4i()
      var sv_ext1 = Vector4i()
      var sv_ext2 = Vector4i()

      var value = 0f
      if (inSum <= 1) run {
        //We're inside the pentachoron (4-Simplex) at (0,0,0,0)

        //Determine which two of (0,0,0,1), (0,0,1,0), (0,1,0,0), (1,0,0,0) are closest.
        var aPoint: Byte = 0x01
        var aScore = ins.x
        var bPoint: Byte = 0x02
        var bScore = ins.y
        if (aScore >= bScore && ins.z > bScore) {
          bScore = ins.z
          bPoint = 0x04
        } else if (aScore < bScore && ins.z > aScore) {
          aScore = ins.z
          aPoint = 0x04
        }
        if (aScore >= bScore && ins.w > bScore) {
          bScore = ins.w
          bPoint = 0x08
        } else if (aScore < bScore && ins.w > aScore) {
          aScore = ins.w
          aPoint = 0x08
        }

        //Now we determine the three lattice points not part of the pentachoron that may contribute.
        //This depends on the closest two pentachoron vertices, including (0,0,0,0)
        val insu = 1 - inSum
        if (insu > aScore || insu > bScore) { //(0,0,0,0) is one of the closest two pentachoron vertices.
          val c = if (bScore > aScore) bPoint else aPoint //Our other closest vertex is the closest out of a and b.
          if (c.toInt() and 0x01 == 0) {
            sv_ext0.x = sb.x.toInt() - 1
            sv_ext2.x = sb.x.toInt()
            sv_ext1.x = sv_ext2.x
            d_ext0.x = d0.x + 1
            d_ext2.x = d0.x
            d_ext1.x = d_ext2.x
          } else {
            sv_ext2.x = sb.x.toInt() + 1
            sv_ext1.x = sv_ext2.x
            sv_ext0.x = sv_ext1.x
            d_ext2.x = d0.x - 1
            d_ext1.x = d_ext2.x
            d_ext0.x = d_ext1.x
          }

          if (c.toInt() and 0x02 == 0) {
            sv_ext2.y = sb.y.toInt()
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext2.y = d0.y
            d_ext1.y = d_ext2.y
            d_ext0.y = d_ext1.y
            if (c.toInt() and 0x01 == 0x01) {
              sv_ext0.y -= 1
              d_ext0.y += 1.0f
            } else {
              sv_ext1.y -= 1
              d_ext1.y += 1.0f
            }
          } else {
            sv_ext2.y = sb.y.toInt() + 1
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext2.y = d0.y - 1
            d_ext1.y = d_ext2.y
            d_ext0.y = d_ext1.y
          }

          if (c.toInt() and 0x04 == 0) {
            sv_ext2.z = sb.z.toInt()
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext2.z = d0.z
            d_ext1.z = d_ext2.z
            d_ext0.z = d_ext1.z
            if (c.toInt() and 0x03 != 0) {
              if (c.toInt() and 0x03 == 0x03) {
                sv_ext0.z -= 1
                d_ext0.z += 1.0f
              } else {
                sv_ext1.z -= 1
                d_ext1.z += 1.0f
              }
            } else {
              sv_ext2.z -= 1
              d_ext2.z += 1.0f
            }
          } else {
            sv_ext2.z = sb.z.toInt() + 1
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext2.z = d0.z - 1
            d_ext1.z = d_ext2.z
            d_ext0.z = d_ext1.z
          }

          if (c.toInt() and 0x08 == 0) {
            sv_ext1.w = sb.w.toInt()
            sv_ext0.w = sv_ext1.w
            sv_ext2.w = sb.w.toInt() - 1
            d_ext1.w = d0.w
            d_ext0.w = d_ext1.w
            d_ext2.w = d0.w + 1
          } else {
            sv_ext2.w = sb.w.toInt() + 1
            sv_ext1.w = sv_ext2.w
            sv_ext0.w = sv_ext1.w
            d_ext2.w = d0.w - 1
            d_ext1.w = d_ext2.w
            d_ext0.w = d_ext1.w
          }
        } else { //(0,0,0,0) is not one of the closest two pentachoron vertices.
          val c = (aPoint.toInt() or bPoint.toInt()).toByte() //Our three extra vertices are determined by the closest two.

          if (c.toInt() and 0x01 == 0) {
            sv_ext2.x = sb.x.toInt()
            sv_ext0.x = sv_ext2.x
            sv_ext1.x = sb.x.toInt() - 1
            d_ext0.x = d0.x - 2 * SQUISH_CONSTANT_4D
            d_ext1.x = d0.x + 1 - SQUISH_CONSTANT_4D
            d_ext2.x = d0.x - SQUISH_CONSTANT_4D
          } else {
            sv_ext2.x = sb.x.toInt() + 1
            sv_ext1.x = sv_ext2.x
            sv_ext0.x = sv_ext1.x
            d_ext0.x = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.x = d0.x - 1.0f - SQUISH_CONSTANT_4D
            d_ext1.x = d_ext2.x
          }

          if (c.toInt() and 0x02 == 0) {
            sv_ext2.y = sb.y.toInt()
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext0.y = d0.y - 2 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - SQUISH_CONSTANT_4D
            d_ext1.y = d_ext2.y
            if (c.toInt() and 0x01 == 0x01) {
              sv_ext1.y -= 1
              d_ext1.y += 1.0f
            } else {
              sv_ext2.y -= 1
              d_ext2.y += 1.0f
            }
          } else {
            sv_ext2.y = sb.y.toInt() + 1
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext0.y = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - 1.0f - SQUISH_CONSTANT_4D
            d_ext1.y = d_ext2.y
          }

          if (c.toInt() and 0x04 == 0) {
            sv_ext2.z = sb.z.toInt()
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext0.z = d0.z - 2 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - SQUISH_CONSTANT_4D
            d_ext1.z = d_ext2.z
            if (c.toInt() and 0x03 == 0x03) {
              sv_ext1.z -= 1
              d_ext1.z += 1.0f
            } else {
              sv_ext2.z -= 1
              d_ext2.z += 1.0f
            }
          } else {
            sv_ext2.z = sb.z.toInt() + 1
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext0.z = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - 1.0f - SQUISH_CONSTANT_4D
            d_ext1.z = d_ext2.z
          }

          if (c.toInt() and 0x08 == 0) {
            sv_ext1.w = sb.w.toInt()
            sv_ext0.w = sv_ext1.w
            sv_ext2.w = sb.w.toInt() - 1
            d_ext0.w = d0.w - 2 * SQUISH_CONSTANT_4D
            d_ext1.w = d0.w - SQUISH_CONSTANT_4D
            d_ext2.w = d0.w + 1 - SQUISH_CONSTANT_4D
          } else {
            sv_ext2.w = sb.w.toInt() + 1
            sv_ext1.w = sv_ext2.w
            sv_ext0.w = sv_ext1.w
            d_ext0.w = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.w = d0.w - 1.0f - SQUISH_CONSTANT_4D
            d_ext1.w = d_ext2.w
          }
        }

        //Contribution (0,0,0,0)
        var attn0 = 2.0f - d0.x * d0.x - d0.y * d0.y - d0.z * d0.z - d0.w * d0.w
        if (attn0 > 0) {
          attn0 *= attn0
          value += attn0 * attn0 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 0, d0.x, d0.y, d0.z, d0.w)
        }

        //Contribution (1,0,0,0)
        val dx1 = d0.x - 1.0f - SQUISH_CONSTANT_4D
        val dy1 = d0.y - 0f - SQUISH_CONSTANT_4D
        val dz1 = d0.z - 0f - SQUISH_CONSTANT_4D
        val dw1 = d0.w - 0f - SQUISH_CONSTANT_4D
        var attn1 = 2.0f - dx1 * dx1 - dy1 * dy1 - dz1 * dz1 - dw1 * dw1
        if (attn1 > 0) {
          attn1 *= attn1
          value += attn1 * attn1 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 0, dx1, dy1, dz1, dw1)
        }

        //Contribution (0,1,0,0)
        val dx2 = d0.x - 0f - SQUISH_CONSTANT_4D
        val dy2 = d0.y - 1.0f - SQUISH_CONSTANT_4D
        var attn2 = 2.0f - dx2 * dx2 - dy2 * dy2 - dz1 * dz1 - dw1 * dw1
        if (attn2 > 0) {
          attn2 *= attn2
          value += attn2 * attn2 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 0, dx2, dy2, dz1, dw1)
        }

        //Contribution (0,0,1,0)
        val dz3 = d0.z - 1.0f - SQUISH_CONSTANT_4D
        var attn3 = 2.0f - dx2 * dx2 - dy1 * dy1 - dz3 * dz3 - dw1 * dw1
        if (attn3 > 0) {
          attn3 *= attn3
          value += attn3 * attn3 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 0, dx2, dy1, dz3, dw1)
        }

        //Contribution (0,0,0,1)
        val dw4 = d0.w - 1.0f - SQUISH_CONSTANT_4D
        var attn4 = 2.0f - dx2 * dx2 - dy1 * dy1 - dz1 * dz1 - dw4 * dw4
        if (attn4 > 0) {
          attn4 *= attn4
          value += attn4 * attn4 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 1, dx2, dy1, dz1, dw4)
        }
      } else if (inSum >= 3) run {
        //We're inside the pentachoron (4-Simplex) at (1,1,1,1)
        //Determine which two of (1,1,1,0), (1,1,0,1), (1,0,1,1), (0,1,1,1) are closest.
        var aPoint: Byte = 0x0E
        var aScore = ins.x
        var bPoint: Byte = 0x0D
        var bScore = ins.y
        if (aScore <= bScore && ins.z < bScore) {
          bScore = ins.z
          bPoint = 0x0B
        } else if (aScore > bScore && ins.z < aScore) {
          aScore = ins.z
          aPoint = 0x0B
        }
        if (aScore <= bScore && ins.w < bScore) {
          bScore = ins.w
          bPoint = 0x07
        } else if (aScore > bScore && ins.w < aScore) {
          aScore = ins.w
          aPoint = 0x07
        }

        //Now we determine the three lattice points not part of the pentachoron that may contribute.
        //This depends on the closest two pentachoron vertices, including (0,0,0,0)
        val insu = 4 - inSum
        if (insu < aScore || insu < bScore) { //(1,1,1,1) is one of the closest two pentachoron vertices.
          val c = if (bScore < aScore) bPoint else aPoint //Our other closest vertex is the closest out of a and b.

          if (c.toInt() and 0x01 != 0) {
            sv_ext0.x = sb.x.toInt() + 2
            sv_ext2.x = sb.x.toInt() + 1
            sv_ext1.x = sv_ext2.x
            d_ext0.x = d0.x - 2.0f - 4 * SQUISH_CONSTANT_4D
            d_ext2.x = d0.x - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext1.x = d_ext2.x
          } else {
            sv_ext2.x = sb.x.toInt()
            sv_ext1.x = sv_ext2.x
            sv_ext0.x = sv_ext1.x
            d_ext2.x = d0.x - 4 * SQUISH_CONSTANT_4D
            d_ext1.x = d_ext2.x
            d_ext0.x = d_ext1.x
          }

          if (c.toInt() and 0x02 != 0) {
            sv_ext2.y = sb.y.toInt() + 1
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext2.y = d0.y - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext1.y = d_ext2.y
            d_ext0.y = d_ext1.y
            if (c.toInt() and 0x01 != 0) {
              sv_ext1.y += 1
              d_ext1.y -= 1.0f
            } else {
              sv_ext0.y += 1
              d_ext0.y -= 1.0f
            }
          } else {
            sv_ext2.y = sb.y.toInt()
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext2.y = d0.y - 4 * SQUISH_CONSTANT_4D
            d_ext1.y = d_ext2.y
            d_ext0.y = d_ext1.y
          }

          if (c.toInt() and 0x04 != 0) {
            sv_ext2.z = sb.z.toInt() + 1
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext2.z = d0.z - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext1.z = d_ext2.z
            d_ext0.z = d_ext1.z
            if (c.toInt() and 0x03 != 0x03) {
              if (c.toInt() and 0x03 == 0) {
                sv_ext0.z += 1
                d_ext0.z -= 1.0f
              } else {
                sv_ext1.z += 1
                d_ext1.z -= 1.0f
              }
            } else {
              sv_ext2.z += 1
              d_ext2.z -= 1.0f
            }
          } else {
            sv_ext2.z = sb.z.toInt()
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext2.z = d0.z - 4 * SQUISH_CONSTANT_4D
            d_ext1.z = d_ext2.z
            d_ext0.z = d_ext1.z
          }

          if (c.toInt() and 0x08 != 0) {
            sv_ext1.w = sb.w.toInt() + 1
            sv_ext0.w = sv_ext1.w
            sv_ext2.w = sb.w.toInt() + 2
            d_ext1.w = d0.w - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext0.w = d_ext1.w
            d_ext2.w = d0.w - 2.0f - 4 * SQUISH_CONSTANT_4D
          } else {
            sv_ext2.w = sb.w.toInt()
            sv_ext1.w = sv_ext2.w
            sv_ext0.w = sv_ext1.w
            d_ext2.w = d0.w - 4 * SQUISH_CONSTANT_4D
            d_ext1.w = d_ext2.w
            d_ext0.w = d_ext1.w
          }
        } else { //(1,1,1,1) is not one of the closest two pentachoron vertices.
          val c = (aPoint.toInt() and bPoint.toInt()).toByte() //Our three extra vertices are determined by the closest two.

          if (c.toInt() and 0x01 != 0) {
            sv_ext2.x = sb.x.toInt() + 1
            sv_ext0.x = sv_ext2.x
            sv_ext1.x = sb.x.toInt() + 2
            d_ext0.x = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext1.x = d0.x - 2.0f - 3 * SQUISH_CONSTANT_4D
            d_ext2.x = d0.x - 1.0f - 3 * SQUISH_CONSTANT_4D
          } else {
            sv_ext2.x = sb.x.toInt()
            sv_ext1.x = sv_ext2.x
            sv_ext0.x = sv_ext1.x
            d_ext0.x = d0.x - 2 * SQUISH_CONSTANT_4D
            d_ext2.x = d0.x - 3 * SQUISH_CONSTANT_4D
            d_ext1.x = d_ext2.x
          }

          if (c.toInt() and 0x02 != 0) {
            sv_ext2.y = sb.y.toInt() + 1
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext0.y = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - 1.0f - 3 * SQUISH_CONSTANT_4D
            d_ext1.y = d_ext2.y
            if (c.toInt() and 0x01 != 0) {
              sv_ext2.y += 1
              d_ext2.y -= 1.0f
            } else {
              sv_ext1.y += 1
              d_ext1.y -= 1.0f
            }
          } else {
            sv_ext2.y = sb.y.toInt()
            sv_ext1.y = sv_ext2.y
            sv_ext0.y = sv_ext1.y
            d_ext0.y = d0.y - 2 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - 3 * SQUISH_CONSTANT_4D
            d_ext1.y = d_ext2.y
          }

          if (c.toInt() and 0x04 != 0) {
            sv_ext2.z = sb.z.toInt() + 1
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext0.z = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - 1.0f - 3 * SQUISH_CONSTANT_4D
            d_ext1.z = d_ext2.z
            if (c.toInt() and 0x03 != 0) {
              sv_ext2.z += 1
              d_ext2.z -= 1.0f
            } else {
              sv_ext1.z += 1
              d_ext1.z -= 1.0f
            }
          } else {
            sv_ext2.z = sb.z.toInt()
            sv_ext1.z = sv_ext2.z
            sv_ext0.z = sv_ext1.z
            d_ext0.z = d0.z - 2 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - 3 * SQUISH_CONSTANT_4D
            d_ext1.z = d_ext2.z
          }

          if (c.toInt() and 0x08 != 0) {
            sv_ext1.w = sb.w.toInt() + 1
            sv_ext0.w = sv_ext1.w
            sv_ext2.w = sb.w.toInt() + 2
            d_ext0.w = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext1.w = d0.w - 1.0f - 3 * SQUISH_CONSTANT_4D
            d_ext2.w = d0.w - 2.0f - 3 * SQUISH_CONSTANT_4D
          } else {
            sv_ext2.w = sb.w.toInt()
            sv_ext1.w = sv_ext2.w
            sv_ext0.w = sv_ext1.w
            d_ext0.w = d0.w - 2 * SQUISH_CONSTANT_4D
            d_ext2.w = d0.w - 3 * SQUISH_CONSTANT_4D
            d_ext1.w = d_ext2.w
          }
        }

        //Contribution (1,1,1,0)
        val dx4 = d0.x - 1.0f - 3 * SQUISH_CONSTANT_4D
        val dy4 = d0.y - 1.0f - 3 * SQUISH_CONSTANT_4D
        val dz4 = d0.z - 1.0f - 3 * SQUISH_CONSTANT_4D
        val dw4 = d0.w - 3 * SQUISH_CONSTANT_4D
        var attn4 = 2.0f - dx4 * dx4 - dy4 * dy4 - dz4 * dz4 - dw4 * dw4
        if (attn4 > 0) {
          attn4 *= attn4
          value += attn4 * attn4 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 0, dx4, dy4, dz4, dw4)
        }

        //Contribution (1,1,0,1)
        val dz3 = d0.z - 3 * SQUISH_CONSTANT_4D
        val dw3 = d0.w - 1.0f - 3 * SQUISH_CONSTANT_4D
        var attn3 = 2.0f - dx4 * dx4 - dy4 * dy4 - dz3 * dz3 - dw3 * dw3
        if (attn3 > 0) {
          attn3 *= attn3
          value += attn3 * attn3 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 1, dx4, dy4, dz3, dw3)
        }

        //Contribution (1,0,1,1)
        val dy2 = d0.y - 3 * SQUISH_CONSTANT_4D
        var attn2 = 2.0f - dx4 * dx4 - dy2 * dy2 - dz4 * dz4 - dw3 * dw3
        if (attn2 > 0) {
          attn2 *= attn2
          value += attn2 * attn2 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 1, dx4, dy2, dz4, dw3)
        }

        //Contribution (0,1,1,1)
        val dx1 = d0.x - 3 * SQUISH_CONSTANT_4D
        var attn1 = 2.0f - dx1 * dx1 - dy4 * dy4 - dz4 * dz4 - dw3 * dw3
        if (attn1 > 0) {
          attn1 *= attn1
          value += attn1 * attn1 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 1, dx1, dy4, dz4, dw3)
        }

        //Contribution (1,1,1,1)
        d0.x = d0.x - 1.0f - 4 * SQUISH_CONSTANT_4D
        d0.y = d0.y - 1.0f - 4 * SQUISH_CONSTANT_4D
        d0.z = d0.z - 1.0f - 4 * SQUISH_CONSTANT_4D
        d0.w = d0.w - 1.0f - 4 * SQUISH_CONSTANT_4D
        var attn0 = 2.0f - d0.x * d0.x - d0.y * d0.y - d0.z * d0.z - d0.w * d0.w
        if (attn0 > 0) {
          attn0 *= attn0
          value += attn0 * attn0 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 1, d0.x, d0.y, d0.z, d0.w)
        }
      } else if (inSum <= 2) run {
        //We're inside the first dispentachoron (Rectified 4-Simplex)
        var aScore: Float
        var aPoint: Byte
        var asbIiggerSide = true
        var bScore: Float
        var bPoint: Byte
        var bsbIiggerSide = true

        //Decide between (1,1,0,0) and (0,0,1,1)
        if (ins.x + ins.y > ins.z + ins.w) {
          aScore = ins.x + ins.y
          aPoint = 0x03
        } else {
          aScore = ins.z + ins.w
          aPoint = 0x0C
        }

        //Decide between (1,0,1,0) and (0,1,0,1)
        if (ins.x + ins.z > ins.y + ins.w) {
          bScore = ins.x + ins.z
          bPoint = 0x05
        } else {
          bScore = ins.y + ins.w
          bPoint = 0x0A
        }

        //Closer between (1,0,0,1) and (0,1,1,0) will replace the further of a and b, if closer.
        if (ins.x + ins.w > ins.y + ins.z) {
          val score = ins.x + ins.w
          if (aScore >= bScore && score > bScore) {
            bScore = score
            bPoint = 0x09
          } else if (aScore < bScore && score > aScore) {
            aScore = score
            aPoint = 0x09
          }
        } else {
          val score = ins.y + ins.z
          if (aScore >= bScore && score > bScore) {
            bScore = score
            bPoint = 0x06
          } else if (aScore < bScore && score > aScore) {
            aScore = score
            aPoint = 0x06
          }
        }

        //Decide if (1,0,0,0) is closer.
        val p1 = 2 - inSum + ins.x
        if (aScore >= bScore && p1 > bScore) {
          bScore = p1
          bPoint = 0x01
          bsbIiggerSide = false
        } else if (aScore < bScore && p1 > aScore) {
          aScore = p1
          aPoint = 0x01
          asbIiggerSide = false
        }

        //Decide if (0,1,0,0) is closer.
        val p2 = 2 - inSum + ins.y
        if (aScore >= bScore && p2 > bScore) {
          bScore = p2
          bPoint = 0x02
          bsbIiggerSide = false
        } else if (aScore < bScore && p2 > aScore) {
          aScore = p2
          aPoint = 0x02
          asbIiggerSide = false
        }

        //Decide if (0,0,1,0) is closer.
        val p3 = 2 - inSum + ins.z
        if (aScore >= bScore && p3 > bScore) {
          bScore = p3
          bPoint = 0x04
          bsbIiggerSide = false
        } else if (aScore < bScore && p3 > aScore) {
          aScore = p3
          aPoint = 0x04
          asbIiggerSide = false
        }

        //Decide if (0,0,0,1) is closer.
        val p4 = 2 - inSum + ins.w
        if (aScore >= bScore && p4 > bScore) {
          bScore = p4
          bPoint = 0x08
          bsbIiggerSide = false
        } else if (aScore < bScore && p4 > aScore) {
          aScore = p4
          aPoint = 0x08
          asbIiggerSide = false
        }

        //Where each of the two closest points are determines how the extra three vertices are calculated.
        if (asbIiggerSide == bsbIiggerSide) {
          if (asbIiggerSide) { //Both closest points on the bigger side
            val c1 = (aPoint.toInt() or bPoint.toInt()).toByte()
            val c2 = (aPoint.toInt() and bPoint.toInt()).toByte()
            if (c1.toInt() and 0x01 == 0) {
              sv_ext0.x = sb.x.toInt()
              sv_ext1.x = sb.x.toInt() - 1
              d_ext0.x = d0.x - 3 * SQUISH_CONSTANT_4D
              d_ext1.x = d0.x + 1 - 2 * SQUISH_CONSTANT_4D
            } else {
              sv_ext1.x = sb.x.toInt() + 1
              sv_ext0.x = sv_ext1.x
              d_ext0.x = d0.x - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext1.x = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
            }

            if (c1.toInt() and 0x02 == 0) {
              sv_ext0.y = sb.y.toInt()
              sv_ext1.y = sb.y.toInt() - 1
              d_ext0.y = d0.y - 3 * SQUISH_CONSTANT_4D
              d_ext1.y = d0.y + 1 - 2 * SQUISH_CONSTANT_4D
            } else {
              sv_ext1.y = sb.y.toInt() + 1
              sv_ext0.y = sv_ext1.y
              d_ext0.y = d0.y - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext1.y = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
            }

            if (c1.toInt() and 0x04 == 0) {
              sv_ext0.z = sb.z.toInt()
              sv_ext1.z = sb.z.toInt() - 1
              d_ext0.z = d0.z - 3 * SQUISH_CONSTANT_4D
              d_ext1.z = d0.z + 1 - 2 * SQUISH_CONSTANT_4D
            } else {
              sv_ext1.z = sb.z.toInt() + 1
              sv_ext0.z = sv_ext1.z
              d_ext0.z = d0.z - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext1.z = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
            }

            if (c1.toInt() and 0x08 == 0) {
              sv_ext0.w = sb.w.toInt()
              sv_ext1.w = sb.w.toInt() - 1
              d_ext0.w = d0.w - 3 * SQUISH_CONSTANT_4D
              d_ext1.w = d0.w + 1 - 2 * SQUISH_CONSTANT_4D
            } else {
              sv_ext1.w = sb.w.toInt() + 1
              sv_ext0.w = sv_ext1.w
              d_ext0.w = d0.w - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext1.w = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
            }

            //One combination is a permutation of (0,0,0,2) based on c2
            sv_ext2 = sb.toVector4i()
  //          sv_ext2.x = sb.x
  //          sv_ext2.y = sb.y
  //          sv_ext2.z = sb.z
  //          sv_ext2.w = sb.w
            d_ext2.x = d0.x - 2 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - 2 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - 2 * SQUISH_CONSTANT_4D
            d_ext2.w = d0.w - 2 * SQUISH_CONSTANT_4D
            if (c2.toInt() and 0x01 != 0) {
              sv_ext2.x += 2
              d_ext2.x -= 2.0f
            } else if (c2.toInt() and 0x02 != 0) {
              sv_ext2.y += 2
              d_ext2.y -= 2.0f
            } else if (c2.toInt() and 0x04 != 0) {
              sv_ext2.z += 2
              d_ext2.z -= 2.0f
            } else {
              sv_ext2.w += 2
              d_ext2.w -= 2.0f
            }

          } else { //Both closest points on the smaller side
            //One of the two extra points is (0,0,0,0)
            sv_ext2 = sb.toVector4i()
  //          sv_ext2.x = sb.x
  //          sv_ext2.y = sb.y
  //          sv_ext2.z = sb.z
  //          sv_ext2.w = sb.w
            d_ext2 = d0
  //          d_ext2.x = d0.x
  //          d_ext2.y = d0.y
  //          d_ext2.z = d0.z
  //          d_ext2.w = d0.w

            //Other two points are based on the omitted axes.
            val c = (aPoint.toInt() or bPoint.toInt()).toByte()

            if (c.toInt() and 0x01 == 0) {
              sv_ext0.x = sb.x.toInt() - 1
              sv_ext1.x = sb.x.toInt()
              d_ext0.x = d0.x + 1 - SQUISH_CONSTANT_4D
              d_ext1.x = d0.x - SQUISH_CONSTANT_4D
            } else {
              sv_ext1.x = sb.x.toInt() + 1
              sv_ext0.x = sv_ext1.x
              d_ext1.x = d0.x - 1.0f - SQUISH_CONSTANT_4D
              d_ext0.x = d_ext1.x
            }

            if (c.toInt() and 0x02 == 0) {
              sv_ext1.y = sb.y.toInt()
              sv_ext0.y = sv_ext1.y
              d_ext1.y = d0.y - SQUISH_CONSTANT_4D
              d_ext0.y = d_ext1.y
              if (c.toInt() and 0x01 == 0x01) {
                sv_ext0.y -= 1
                d_ext0.y += 1.0f
              } else {
                sv_ext1.y -= 1
                d_ext1.y += 1.0f
              }
            } else {
              sv_ext1.y = sb.y.toInt() + 1
              sv_ext0.y = sv_ext1.y
              d_ext1.y = d0.y - 1.0f - SQUISH_CONSTANT_4D
              d_ext0.y = d_ext1.y
            }

            if (c.toInt() and 0x04 == 0) {
              sv_ext1.z = sb.z.toInt()
              sv_ext0.z = sv_ext1.z
              d_ext1.z = d0.z - SQUISH_CONSTANT_4D
              d_ext0.z = d_ext1.z
              if (c.toInt() and 0x03 == 0x03) {
                sv_ext0.z -= 1
                d_ext0.z += 1.0f
              } else {
                sv_ext1.z -= 1
                d_ext1.z += 1.0f
              }
            } else {
              sv_ext1.z = sb.z.toInt() + 1
              sv_ext0.z = sv_ext1.z
              d_ext1.z = d0.z - 1.0f - SQUISH_CONSTANT_4D
              d_ext0.z = d_ext1.z
            }

            if (c.toInt() and 0x08 == 0) {
              sv_ext0.w = sb.w.toInt()
              sv_ext1.w = sb.w.toInt() - 1
              d_ext0.w = d0.w - SQUISH_CONSTANT_4D
              d_ext1.w = d0.w + 1 - SQUISH_CONSTANT_4D
            } else {
              sv_ext1.w = sb.w.toInt() + 1
              sv_ext0.w = sv_ext1.w
              d_ext1.w = d0.w - 1.0f - SQUISH_CONSTANT_4D
              d_ext0.w = d_ext1.w
            }

          }
        } else { //One point on each "side"
          val c1: Byte
          val c2: Byte
          if (asbIiggerSide) {
            c1 = aPoint
            c2 = bPoint
          } else {
            c1 = bPoint
            c2 = aPoint
          }

          //Two contributions are the bigger-sided point with each 0 replaced with -1.
          if (c1.toInt() and 0x01 == 0) {
            sv_ext0.x = sb.x.toInt() - 1
            sv_ext1.x = sb.x.toInt()
            d_ext0.x = d0.x + 1 - SQUISH_CONSTANT_4D
            d_ext1.x = d0.x - SQUISH_CONSTANT_4D
          } else {
            sv_ext1.x = sb.x.toInt() + 1
            sv_ext0.x = sv_ext1.x
            d_ext1.x = d0.x - 1.0f - SQUISH_CONSTANT_4D
            d_ext0.x = d_ext1.x
          }

          if (c1.toInt() and 0x02 == 0) {
            sv_ext1.y = sb.y.toInt()
            sv_ext0.y = sv_ext1.y
            d_ext1.y = d0.y - SQUISH_CONSTANT_4D
            d_ext0.y = d_ext1.y
            if (c1.toInt() and 0x01 == 0x01) {
              sv_ext0.y -= 1
              d_ext0.y += 1.0f
            } else {
              sv_ext1.y -= 1
              d_ext1.y += 1.0f
            }
          } else {
            sv_ext1.y = sb.y.toInt() + 1
            sv_ext0.y = sv_ext1.y
            d_ext1.y = d0.y - 1.0f - SQUISH_CONSTANT_4D
            d_ext0.y = d_ext1.y
          }

          if (c1.toInt() and 0x04 == 0) {
            sv_ext1.z = sb.z.toInt()
            sv_ext0.z = sv_ext1.z
            d_ext1.z = d0.z - SQUISH_CONSTANT_4D
            d_ext0.z = d_ext1.z
            if (c1.toInt() and 0x03 == 0x03) {
              sv_ext0.z -= 1
              d_ext0.z += 1.0f
            } else {
              sv_ext1.z -= 1
              d_ext1.z += 1.0f
            }
          } else {
            sv_ext1.z = sb.z.toInt() + 1
            sv_ext0.z = sv_ext1.z
            d_ext1.z = d0.z - 1.0f - SQUISH_CONSTANT_4D
            d_ext0.z = d_ext1.z
          }

          if (c1.toInt() and 0x08 == 0) {
            sv_ext0.w = sb.w.toInt()
            sv_ext1.w = sb.w.toInt() - 1
            d_ext0.w = d0.w - SQUISH_CONSTANT_4D
            d_ext1.w = d0.w + 1 - SQUISH_CONSTANT_4D
          } else {
            sv_ext1.w = sb.w.toInt() + 1
            sv_ext0.w = sv_ext1.w
            d_ext1.w = d0.w - 1.0f - SQUISH_CONSTANT_4D
            d_ext0.w = d_ext1.w
          }

          //One contribution is a permutation of (0,0,0,2) based on the smaller-sided point
          sv_ext2 = sb.toVector4i()
          d_ext2.x = d0.x - 2 * SQUISH_CONSTANT_4D
          d_ext2.y = d0.y - 2 * SQUISH_CONSTANT_4D
          d_ext2.z = d0.z - 2 * SQUISH_CONSTANT_4D
          d_ext2.w = d0.w - 2 * SQUISH_CONSTANT_4D
          if (c2.toInt() and 0x01 != 0) {
            sv_ext2.x += 2
            d_ext2.x -= 2.0f
          } else if (c2.toInt() and 0x02 != 0) {
            sv_ext2.y += 2
            d_ext2.y -= 2.0f
          } else if (c2.toInt() and 0x04 != 0) {
            sv_ext2.z += 2
            d_ext2.z -= 2.0f
          } else {
            sv_ext2.w += 2
            d_ext2.w -= 2.0f
          }
        }

        //Contribution (1,0,0,0)
        val dx1 = d0.x - 1.0f - SQUISH_CONSTANT_4D
        val dy1 = d0.y - 0f - SQUISH_CONSTANT_4D
        val dz1 = d0.z - 0f - SQUISH_CONSTANT_4D
        val dw1 = d0.w - 0f - SQUISH_CONSTANT_4D
        var attn1 = 2.0f - dx1 * dx1 - dy1 * dy1 - dz1 * dz1 - dw1 * dw1
        if (attn1 > 0) {
          attn1 *= attn1
          value += attn1 * attn1 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 0, dx1, dy1, dz1, dw1)
        }

        //Contribution (0,1,0,0)
        val dx2 = d0.x - 0f - SQUISH_CONSTANT_4D
        val dy2 = d0.y - 1.0f - SQUISH_CONSTANT_4D
        var attn2 = 2.0f - dx2 * dx2 - dy2 * dy2 - dz1 * dz1 - dw1 * dw1
        if (attn2 > 0) {
          attn2 *= attn2
          value += attn2 * attn2 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 0, dx2, dy2, dz1, dw1)
        }

        //Contribution (0,0,1,0)
        val dz3 = d0.z - 1.0f - SQUISH_CONSTANT_4D
        var attn3 = 2.0f - dx2 * dx2 - dy1 * dy1 - dz3 * dz3 - dw1 * dw1
        if (attn3 > 0) {
          attn3 *= attn3
          value += attn3 * attn3 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 0, dx2, dy1, dz3, dw1)
        }

        //Contribution (0,0,0,1)
        val dw4 = d0.w - 1.0f - SQUISH_CONSTANT_4D
        var attn4 = 2.0f - dx2 * dx2 - dy1 * dy1 - dz1 * dz1 - dw4 * dw4
        if (attn4 > 0) {
          attn4 *= attn4
          value += attn4 * attn4 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 1, dx2, dy1, dz1, dw4)
        }

        //Contribution (1,1,0,0)
        val dx5 = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dy5 = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dz5 = d0.z - 0f - 2 * SQUISH_CONSTANT_4D
        val dw5 = d0.w - 0f - 2 * SQUISH_CONSTANT_4D
        var attn5 = 2.0f - dx5 * dx5 - dy5 * dy5 - dz5 * dz5 - dw5 * dw5
        if (attn5 > 0) {
          attn5 *= attn5
          value += attn5 * attn5 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 0, dx5, dy5, dz5, dw5)
        }

        //Contribution (1,0,1,0)
        val dx6 = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dy6 = d0.y - 0f - 2 * SQUISH_CONSTANT_4D
        val dz6 = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dw6 = d0.w - 0f - 2 * SQUISH_CONSTANT_4D
        var attn6 = 2.0f - dx6 * dx6 - dy6 * dy6 - dz6 * dz6 - dw6 * dw6
        if (attn6 > 0) {
          attn6 *= attn6
          value += attn6 * attn6 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 0, dx6, dy6, dz6, dw6)
        }

        //Contribution (1,0,0,1)
        val dx7 = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dy7 = d0.y - 0f - 2 * SQUISH_CONSTANT_4D
        val dz7 = d0.z - 0f - 2 * SQUISH_CONSTANT_4D
        val dw7 = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
        var attn7 = 2.0f - dx7 * dx7 - dy7 * dy7 - dz7 * dz7 - dw7 * dw7
        if (attn7 > 0) {
          attn7 *= attn7
          value += attn7 * attn7 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 1, dx7, dy7, dz7, dw7)
        }

        //Contribution (0,1,1,0)
        val dx8 = d0.x - 0f - 2 * SQUISH_CONSTANT_4D
        val dy8 = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dz8 = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dw8 = d0.w - 0f - 2 * SQUISH_CONSTANT_4D
        var attn8 = 2.0f - dx8 * dx8 - dy8 * dy8 - dz8 * dz8 - dw8 * dw8
        if (attn8 > 0) {
          attn8 *= attn8
          value += attn8 * attn8 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 0, dx8, dy8, dz8, dw8)
        }

        //Contribution (0,1,0,1)
        val dx9 = d0.x - 0f - 2 * SQUISH_CONSTANT_4D
        val dy9 = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dz9 = d0.z - 0f - 2 * SQUISH_CONSTANT_4D
        val dw9 = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
        var attn9 = 2.0f - dx9 * dx9 - dy9 * dy9 - dz9 * dz9 - dw9 * dw9
        if (attn9 > 0) {
          attn9 *= attn9
          value += attn9 * attn9 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 1, dx9, dy9, dz9, dw9)
        }

        //Contribution (0,0,1,1)
        val dx10 = d0.x - 0f - 2 * SQUISH_CONSTANT_4D
        val dy10 = d0.y - 0f - 2 * SQUISH_CONSTANT_4D
        val dz10 = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dw10 = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
        var attn10 = 2.0f - dx10 * dx10 - dy10 * dy10 - dz10 * dz10 - dw10 * dw10
        if (attn10 > 0) {
          attn10 *= attn10
          value += attn10 * attn10 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 1, dx10, dy10, dz10, dw10)
        }
      } else run {
        //We're inside the second dispentachoron (Rectified 4-Simplex)
        var aScore: Float
        var aPoint: Byte
        var asbIiggerSide = true
        var bScore: Float
        var bPoint: Byte
        var bsbIiggerSide = true

        //Decide between (0,0,1,1) and (1,1,0,0)
        if (ins.x + ins.y < ins.z + ins.w) {
          aScore = ins.x + ins.y
          aPoint = 0x0C
        } else {
          aScore = ins.z + ins.w
          aPoint = 0x03
        }

        //Decide between (0,1,0,1) and (1,0,1,0)
        if (ins.x + ins.z < ins.y + ins.w) {
          bScore = ins.x + ins.z
          bPoint = 0x0A
        } else {
          bScore = ins.y + ins.w
          bPoint = 0x05
        }

        //Closer between (0,1,1,0) and (1,0,0,1) will replace the further of a and b, if closer.
        if (ins.x + ins.w < ins.y + ins.z) {
          val score = ins.x + ins.w
          if (aScore <= bScore && score < bScore) {
            bScore = score
            bPoint = 0x06
          } else if (aScore > bScore && score < aScore) {
            aScore = score
            aPoint = 0x06
          }
        } else {
          val score = ins.y + ins.z
          if (aScore <= bScore && score < bScore) {
            bScore = score
            bPoint = 0x09
          } else if (aScore > bScore && score < aScore) {
            aScore = score
            aPoint = 0x09
          }
        }

        //Decide if (0,1,1,1) is closer.
        val p1 = 3 - inSum + ins.x
        if (aScore <= bScore && p1 < bScore) {
          bScore = p1
          bPoint = 0x0E
          bsbIiggerSide = false
        } else if (aScore > bScore && p1 < aScore) {
          aScore = p1
          aPoint = 0x0E
          asbIiggerSide = false
        }

        //Decide if (1,0,1,1) is closer.
        val p2 = 3 - inSum + ins.y
        if (aScore <= bScore && p2 < bScore) {
          bScore = p2
          bPoint = 0x0D
          bsbIiggerSide = false
        } else if (aScore > bScore && p2 < aScore) {
          aScore = p2
          aPoint = 0x0D
          asbIiggerSide = false
        }

        //Decide if (1,1,0,1) is closer.
        val p3 = 3 - inSum + ins.z
        if (aScore <= bScore && p3 < bScore) {
          bScore = p3
          bPoint = 0x0B
          bsbIiggerSide = false
        } else if (aScore > bScore && p3 < aScore) {
          aScore = p3
          aPoint = 0x0B
          asbIiggerSide = false
        }

        //Decide if (1,1,1,0) is closer.
        val p4 = 3 - inSum + ins.w
        if (aScore <= bScore && p4 < bScore) {
          bScore = p4
          bPoint = 0x07
          bsbIiggerSide = false
        } else if (aScore > bScore && p4 < aScore) {
          aScore = p4
          aPoint = 0x07
          asbIiggerSide = false
        }

        //Where each of the two closest points are determines how the extra three vertices are calculated.
        if (asbIiggerSide == bsbIiggerSide) {
          if (asbIiggerSide) { //Both closest points on the bigger side
            val c1 = (aPoint.toInt() and bPoint.toInt()).toByte()
            val c2 = (aPoint.toInt() or bPoint.toInt()).toByte()

            //Two contributions are permutations of (0,0,0,1) and (0,0,0,2) based on c1
            sv_ext1 = sb.toVector4i()
            sv_ext0 = sv_ext1
  //          sv_ext1.y = sb.y
  //          sv_ext0.y = sv_ext1.y
  //          sv_ext1.z = sb.z
  //          sv_ext0.z = sv_ext1.z
  //          sv_ext1.w = sb.w
  //          sv_ext0.w = sv_ext1.w
            d_ext0.x = d0.x - SQUISH_CONSTANT_4D
            d_ext0.y = d0.y - SQUISH_CONSTANT_4D
            d_ext0.z = d0.z - SQUISH_CONSTANT_4D
            d_ext0.w = d0.w - SQUISH_CONSTANT_4D
            d_ext1.x = d0.x - 2 * SQUISH_CONSTANT_4D
            d_ext1.y = d0.y - 2 * SQUISH_CONSTANT_4D
            d_ext1.z = d0.z - 2 * SQUISH_CONSTANT_4D
            d_ext1.w = d0.w - 2 * SQUISH_CONSTANT_4D
            if (c1.toInt() and 0x01 != 0) {
              sv_ext0.x += 1
              d_ext0.x -= 1.0f
              sv_ext1.x += 2
              d_ext1.x -= 2.0f
            } else if (c1.toInt() and 0x02 != 0) {
              sv_ext0.y += 1
              d_ext0.y -= 1.0f
              sv_ext1.y += 2
              d_ext1.y -= 2.0f
            } else if (c1.toInt() and 0x04 != 0) {
              sv_ext0.z += 1
              d_ext0.z -= 1.0f
              sv_ext1.z += 2
              d_ext1.z -= 2.0f
            } else {
              sv_ext0.w += 1
              d_ext0.w -= 1.0f
              sv_ext1.w += 2
              d_ext1.w -= 2.0f
            }

            //One contribution is a permutation of (1,1,1,-1) based on c2
            sv_ext2.x = sb.x.toInt() + 1
            sv_ext2.y = sb.y.toInt() + 1
            sv_ext2.z = sb.z.toInt() + 1
            sv_ext2.w = sb.w.toInt() + 1
            d_ext2.x = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
            d_ext2.w = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
            if (c2.toInt() and 0x01 == 0) {
              sv_ext2.x -= 2
              d_ext2.x += 2.0f
            } else if (c2.toInt() and 0x02 == 0) {
              sv_ext2.y -= 2
              d_ext2.y += 2.0f
            } else if (c2.toInt() and 0x04 == 0) {
              sv_ext2.z -= 2
              d_ext2.z += 2.0f
            } else {
              sv_ext2.w -= 2
              d_ext2.w += 2.0f
            }
          } else { //Both closest points on the smaller side
            //One of the two extra points is (1,1,1,1)
            sv_ext2.x = sb.x.toInt() + 1
            sv_ext2.y = sb.y.toInt() + 1
            sv_ext2.z = sb.z.toInt() + 1
            sv_ext2.w = sb.w.toInt() + 1
            d_ext2.x = d0.x - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext2.y = d0.y - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext2.z = d0.z - 1.0f - 4 * SQUISH_CONSTANT_4D
            d_ext2.w = d0.w - 1.0f - 4 * SQUISH_CONSTANT_4D

            //Other two points are based on the shared axes.
            val c = (aPoint.toInt() and bPoint.toInt()).toByte()

            if (c.toInt() and 0x01 != 0) {
              sv_ext0.x = sb.x.toInt() + 2
              sv_ext1.x = sb.x.toInt() + 1
              d_ext0.x = d0.x - 2.0f - 3 * SQUISH_CONSTANT_4D
              d_ext1.x = d0.x - 1.0f - 3 * SQUISH_CONSTANT_4D
            } else {
              sv_ext1.x = sb.x.toInt()
              sv_ext0.x = sv_ext1.x
              d_ext1.x = d0.x - 3 * SQUISH_CONSTANT_4D
              d_ext0.x = d_ext1.x
            }

            if (c.toInt() and 0x02 != 0) {
              sv_ext1.y = sb.y.toInt() + 1
              sv_ext0.y = sv_ext1.y
              d_ext1.y = d0.y - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext0.y = d_ext1.y
              if (c.toInt() and 0x01 == 0) {
                sv_ext0.y += 1
                d_ext0.y -= 1.0f
              } else {
                sv_ext1.y += 1
                d_ext1.y -= 1.0f
              }
            } else {
              sv_ext1.y = sb.y.toInt()
              sv_ext0.y = sv_ext1.y
              d_ext1.y = d0.y - 3 * SQUISH_CONSTANT_4D
              d_ext0.y = d_ext1.y
            }

            if (c.toInt() and 0x04 != 0) {
              sv_ext1.z = sb.z.toInt() + 1
              sv_ext0.z = sv_ext1.z
              d_ext1.z = d0.z - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext0.z = d_ext1.z
              if (c.toInt() and 0x03 == 0) {
                sv_ext0.z += 1
                d_ext0.z -= 1.0f
              } else {
                sv_ext1.z += 1
                d_ext1.z -= 1.0f
              }
            } else {
              sv_ext1.z = sb.z.toInt()
              sv_ext0.z = sv_ext1.z
              d_ext1.z = d0.z - 3 * SQUISH_CONSTANT_4D
              d_ext0.z = d_ext1.z
            }

            if (c.toInt() and 0x08 != 0) {
              sv_ext0.w = sb.w.toInt() + 1
              sv_ext1.w = sb.w.toInt() + 2
              d_ext0.w = d0.w - 1.0f - 3 * SQUISH_CONSTANT_4D
              d_ext1.w = d0.w - 2.0f - 3 * SQUISH_CONSTANT_4D
            } else {
              sv_ext1.w = sb.w.toInt()
              sv_ext0.w = sv_ext1.w
              d_ext1.w = d0.w - 3 * SQUISH_CONSTANT_4D
              d_ext0.w = d_ext1.w
            }
          }
        } else { //One point on each "side"
          val c1: Byte
          val c2: Byte
          if (asbIiggerSide) {
            c1 = aPoint
            c2 = bPoint
          } else {
            c1 = bPoint
            c2 = aPoint
          }

          //Two contributions are the bigger-sided point with each 1 replaced with 2.
          if (c1.toInt() and 0x01 != 0) {
            sv_ext0.x = sb.x.toInt() + 2
            sv_ext1.x = sb.x.toInt() + 1
            d_ext0.x = d0.x - 2.0f - 3 * SQUISH_CONSTANT_4D
            d_ext1.x = d0.x - 1.0f - 3 * SQUISH_CONSTANT_4D
          } else {
            sv_ext1.x = sb.x.toInt()
            sv_ext0.x = sv_ext1.x
            d_ext1.x = d0.x - 3 * SQUISH_CONSTANT_4D
            d_ext0.x = d_ext1.x
          }

          if (c1.toInt() and 0x02 != 0) {
            sv_ext1.y = sb.y.toInt() + 1
            sv_ext0.y = sv_ext1.y
            d_ext1.y = d0.y - 1.0f - 3 * SQUISH_CONSTANT_4D
            d_ext0.y = d_ext1.y
            if (c1.toInt() and 0x01 == 0) {
              sv_ext0.y += 1
              d_ext0.y -= 1.0f
            } else {
              sv_ext1.y += 1
              d_ext1.y -= 1.0f
            }
          } else {
            sv_ext1.y = sb.y.toInt()
            sv_ext0.y = sv_ext1.y
            d_ext1.y = d0.y - 3 * SQUISH_CONSTANT_4D
            d_ext0.y = d_ext1.y
          }

          if (c1.toInt() and 0x04 != 0) {
            sv_ext1.z = sb.z.toInt() + 1
            sv_ext0.z = sv_ext1.z.toInt()
            d_ext1.z = d0.z - 1.0f - 3 * SQUISH_CONSTANT_4D
            d_ext0.z = d_ext1.z
            if (c1.toInt() and 0x03 == 0) {
              sv_ext0.z += 1
              d_ext0.z -= 1.0f
            } else {
              sv_ext1.z += 1
              d_ext1.z -= 1.0f
            }
          } else {
            sv_ext1.z = sb.z.toInt()
            sv_ext0.z = sv_ext1.z
            d_ext1.z = d0.z - 3 * SQUISH_CONSTANT_4D
            d_ext0.z = d_ext1.z
          }

          if (c1.toInt() and 0x08 != 0) {
            sv_ext0.w = sb.w.toInt() + 1
            sv_ext1.w = sb.w.toInt() + 2
            d_ext0.w = d0.w - 1.0f - 3 * SQUISH_CONSTANT_4D
            d_ext1.w = d0.w - 2.0f - 3 * SQUISH_CONSTANT_4D
          } else {
            sv_ext1.w = sb.w.toInt()
            sv_ext0.w = sv_ext1.w
            d_ext1.w = d0.w - 3 * SQUISH_CONSTANT_4D
            d_ext0.w = d_ext1.w
          }

          //One contribution is a permutation of (1,1,1,-1) based on the smaller-sided point
          sv_ext2 = sb.toVector4i() + 1
          d_ext2.x = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
          d_ext2.y = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
          d_ext2.z = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
          d_ext2.w = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
          if (c2.toInt() and 0x01 == 0) {
            sv_ext2.x -= 2
            d_ext2.x += 2.0f
          } else if (c2.toInt() and 0x02 == 0) {
            sv_ext2.y -= 2
            d_ext2.y += 2.0f
          } else if (c2.toInt() and 0x04 == 0) {
            sv_ext2.z -= 2
            d_ext2.z += 2.0f
          } else {
            sv_ext2.w -= 2
            d_ext2.w += 2.0f
          }
        }

        //Contribution (1,1,1,0)
        val dx4 = d0.x - 1.0f - 3 * SQUISH_CONSTANT_4D
        val dy4 = d0.y - 1.0f - 3 * SQUISH_CONSTANT_4D
        val dz4 = d0.z - 1.0f - 3 * SQUISH_CONSTANT_4D
        val dw4 = d0.w - 3 * SQUISH_CONSTANT_4D
        var attn4 = 2.0f - dx4 * dx4 - dy4 * dy4 - dz4 * dz4 - dw4 * dw4
        if (attn4 > 0) {
          attn4 *= attn4
          value += attn4 * attn4 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 0, dx4, dy4, dz4, dw4)
        }

        //Contribution (1,1,0,1)
        val dz3 = d0.z - 3 * SQUISH_CONSTANT_4D
        val dw3 = d0.w - 1.0f - 3 * SQUISH_CONSTANT_4D
        var attn3 = 2.0f - dx4 * dx4 - dy4 * dy4 - dz3 * dz3 - dw3 * dw3
        if (attn3 > 0) {
          attn3 *= attn3
          value += attn3 * attn3 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 1, dx4, dy4, dz3, dw3)
        }

        //Contribution (1,0,1,1)
        val dy2 = d0.y - 3 * SQUISH_CONSTANT_4D
        var attn2 = 2.0f - dx4 * dx4 - dy2 * dy2 - dz4 * dz4 - dw3 * dw3
        if (attn2 > 0) {
          attn2 *= attn2
          value += attn2 * attn2 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 1, dx4, dy2, dz4, dw3)
        }

        //Contribution (0,1,1,1)
        val dx1 = d0.x - 3 * SQUISH_CONSTANT_4D
        var attn1 = 2.0f - dx1 * dx1 - dy4 * dy4 - dz4 * dz4 - dw3 * dw3
        if (attn1 > 0) {
          attn1 *= attn1
          value += attn1 * attn1 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 1, dx1, dy4, dz4, dw3)
        }

        //Contribution (1,1,0,0)
        val dx5 = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dy5 = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dz5 = d0.z - 0f - 2 * SQUISH_CONSTANT_4D
        val dw5 = d0.w - 0f - 2 * SQUISH_CONSTANT_4D
        var attn5 = 2.0f - dx5 * dx5 - dy5 * dy5 - dz5 * dz5 - dw5 * dw5
        if (attn5 > 0) {
          attn5 *= attn5
          value += attn5 * attn5 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 0, dx5, dy5, dz5, dw5)
        }

        //Contribution (1,0,1,0)
        val dx6 = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dy6 = d0.y - 0f - 2 * SQUISH_CONSTANT_4D
        val dz6 = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dw6 = d0.w - 0f - 2 * SQUISH_CONSTANT_4D
        var attn6 = 2.0f - dx6 * dx6 - dy6 * dy6 - dz6 * dz6 - dw6 * dw6
        if (attn6 > 0) {
          attn6 *= attn6
          value += attn6 * attn6 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 0, dx6, dy6, dz6, dw6)
        }

        //Contribution (1,0,0,1)
        val dx7 = d0.x - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dy7 = d0.y - 0f - 2 * SQUISH_CONSTANT_4D
        val dz7 = d0.z - 0f - 2 * SQUISH_CONSTANT_4D
        val dw7 = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
        var attn7 = 2.0f - dx7 * dx7 - dy7 * dy7 - dz7 * dz7 - dw7 * dw7
        if (attn7 > 0) {
          attn7 *= attn7
          value += attn7 * attn7 * extrapolate(sb.x.toInt() + 1, sb.y.toInt() + 0, sb.z.toInt() + 0, sb.w.toInt() + 1, dx7, dy7, dz7, dw7)
        }

        //Contribution (0,1,1,0)
        val dx8 = d0.x - 0f - 2 * SQUISH_CONSTANT_4D
        val dy8 = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dz8 = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dw8 = d0.w - 0f - 2 * SQUISH_CONSTANT_4D
        var attn8 = 2.0f - dx8 * dx8 - dy8 * dy8 - dz8 * dz8 - dw8 * dw8
        if (attn8 > 0) {
          attn8 *= attn8
          value += attn8 * attn8 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 1, sb.w.toInt() + 0, dx8, dy8, dz8, dw8)
        }

        //Contribution (0,1,0,1)
        val dx9 = d0.x - 0f - 2 * SQUISH_CONSTANT_4D
        val dy9 = d0.y - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dz9 = d0.z - 0f - 2 * SQUISH_CONSTANT_4D
        val dw9 = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
        var attn9 = 2.0f - dx9 * dx9 - dy9 * dy9 - dz9 * dz9 - dw9 * dw9
        if (attn9 > 0) {
          attn9 *= attn9
          value += attn9 * attn9 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 1, sb.z.toInt() + 0, sb.w.toInt() + 1, dx9, dy9, dz9, dw9)
        }

        //Contribution (0,0,1,1)
        val dx10 = d0.x - 0f - 2 * SQUISH_CONSTANT_4D
        val dy10 = d0.y - 0f - 2 * SQUISH_CONSTANT_4D
        val dz10 = d0.z - 1.0f - 2 * SQUISH_CONSTANT_4D
        val dw10 = d0.w - 1.0f - 2 * SQUISH_CONSTANT_4D
        var attn10 = 2.0f - dx10 * dx10 - dy10 * dy10 - dz10 * dz10 - dw10 * dw10
        if (attn10 > 0) {
          attn10 *= attn10
          value += attn10 * attn10 * extrapolate(sb.x.toInt() + 0, sb.y.toInt() + 0, sb.z.toInt() + 1, sb.w.toInt() + 1, dx10, dy10, dz10, dw10)
        }
      }

      //First extra vertex
      var attn_ext0 = 2.0f - d_ext0.x * d_ext0.x - d_ext0.y * d_ext0.y - d_ext0.z * d_ext0.z - d_ext0.w * d_ext0.w
      if (attn_ext0 > 0) {
        attn_ext0 *= attn_ext0
        value += attn_ext0 * attn_ext0 * extrapolate(sv_ext0.x, sv_ext0.y, sv_ext0.z, sv_ext0.w, d_ext0.x, d_ext0.y, d_ext0.z, d_ext0.w)
      }

      //Second extra vertex
      var attn_ext1 = 2.0f - d_ext1.x * d_ext1.x - d_ext1.y * d_ext1.y - d_ext1.z * d_ext1.z - d_ext1.w * d_ext1.w
      if (attn_ext1 > 0) {
        attn_ext1 *= attn_ext1
        value += attn_ext1 * attn_ext1 * extrapolate(sv_ext1.x, sv_ext1.y, sv_ext1.z, sv_ext1.w, d_ext1.x, d_ext1.y, d_ext1.z, d_ext1.w)
      }

      //Third extra vertex
      var attn_ext2 = 2.0f - d_ext2.x * d_ext2.x - d_ext2.y * d_ext2.y - d_ext2.z * d_ext2.z - d_ext2.w * d_ext2.w
      if (attn_ext2 > 0) {
        attn_ext2 *= attn_ext2
        value += attn_ext2 * attn_ext2 * extrapolate(sv_ext2.x, sv_ext2.y, sv_ext2.z, sv_ext2.w, d_ext2.x, d_ext2.y, d_ext2.z, d_ext2.w)
      }

      return value / NORM_CONSTANT_4D
    }
  */
  private fun extrapolate(sbx: Int, sby: Int, dx: Float, dy: Float): Float {
    val index = perm[perm[sbx and 0xFF] + sby and 0xFF].toInt() and 0x0E
    return gradients2D[index] * dx + gradients2D[index + 1] * dy
  }

  private fun extrapolate(sbx: Int, sby: Int, sbz: Int, dx: Float, dy: Float, dz: Float): Float {
    val index = permGradIndex3D[perm[perm[sbx and 0xFF] + sby and 0xFF] + sbz and 0xFF].toInt()
    return (gradients3D[index] * dx
        + gradients3D[index + 1] * dy
        + gradients3D[index + 2] * dz)
  }

  private fun extrapolate(sbx: Int, sby: Int, sbz: Int, sbw: Int, dx: Float, dy: Float, dz: Float, dw: Float): Float {
    val index = perm[perm[perm[perm[sbx and 0xFF] + sby and 0xFF] + sbz and 0xFF] + sbw and 0xFF].toInt() and 0xFC
    return (gradients4D[index] * dx
        + gradients4D[index + 1] * dy
        + gradients4D[index + 2] * dz
        + gradients4D[index + 3] * dw)
  }

  private fun fastFloor(x: Float): Float {
    val xi = x.toInt()
    return (if (x < xi) xi - 1 else xi).toFloat()
  }

  //Gradients for 2D. They approximate the directions to the
  //vertices of an octagon from the center.
  private val gradients2D = byteArrayOf(5, 2, 2, 5, -5, 2, -2, 5, 5, -2, 2, -5, -5, -2, -2, -5)

  //Gradients for 3D. They approximate the directions to the
  //vertices of a rhombicuboctahedron from the center, skewed so
  //that the triangular and square facets can be inscribed inside
  //circles of the same radius.
  private val gradients3D = byteArrayOf(-11, 4, 4, -4, 11, 4, -4, 4, 11, 11, 4, 4, 4, 11, 4, 4, 4, 11, -11, -4, 4, -4, -11, 4, -4, -4, 11, 11, -4, 4, 4, -11, 4, 4, -4, 11, -11, 4, -4, -4, 11, -4, -4, 4, -11, 11, 4, -4, 4, 11, -4, 4, 4, -11, -11, -4, -4, -4, -11, -4, -4, -4, -11, 11, -4, -4, 4, -11, -4, 4, -4, -11)

  //Gradients for 4D. They approximate the directions to the
  //vertices of a disprismatotesseractihexadecachoron from the center,
  //skewed so that the tetrahedral and cubic facets can be inscribed inside
  //spheres of the same radius.
  private val gradients4D = byteArrayOf(3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, -3, 1, 1, 1, -1, 3, 1, 1, -1, 1, 3, 1, -1, 1, 1, 3, 3, -1, 1, 1, 1, -3, 1, 1, 1, -1, 3, 1, 1, -1, 1, 3, -3, -1, 1, 1, -1, -3, 1, 1, -1, -1, 3, 1, -1, -1, 1, 3, 3, 1, -1, 1, 1, 3, -1, 1, 1, 1, -3, 1, 1, 1, -1, 3, -3, 1, -1, 1, -1, 3, -1, 1, -1, 1, -3, 1, -1, 1, -1, 3, 3, -1, -1, 1, 1, -3, -1, 1, 1, -1, -3, 1, 1, -1, -1, 3, -3, -1, -1, 1, -1, -3, -1, 1, -1, -1, -3, 1, -1, -1, -1, 3, 3, 1, 1, -1, 1, 3, 1, -1, 1, 1, 3, -1, 1, 1, 1, -3, -3, 1, 1, -1, -1, 3, 1, -1, -1, 1, 3, -1, -1, 1, 1, -3, 3, -1, 1, -1, 1, -3, 1, -1, 1, -1, 3, -1, 1, -1, 1, -3, -3, -1, 1, -1, -1, -3, 1, -1, -1, -1, 3, -1, -1, -1, 1, -3, 3, 1, -1, -1, 1, 3, -1, -1, 1, 1, -3, -1, 1, 1, -1, -3, -3, 1, -1, -1, -1, 3, -1, -1, -1, 1, -3, -1, -1, 1, -1, -3, 3, -1, -1, -1, 1, -3, -1, -1, 1, -1, -3, -1, 1, -1, -1, -3, -3, -1, -1, -1, -1, -3, -1, -1, -1, -1, -3, -1, -1, -1, -1, -3)
}