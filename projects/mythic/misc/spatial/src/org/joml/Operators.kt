package org.joml

import mythic.spatial.Quaternion

/* Matrix3f */

//operator fun Matrix3f.get(c: Int, r: Int): Float = get(c, r)
operator fun Matrix3f.minus(m: Matrix3f) = sub(m)

operator fun Matrix3f.plus(m: Matrix3fc) = add(m)
operator fun Matrix3f.times(m: Matrix3fc) = mul(m)
operator fun Matrix3f.times(v: Vector3f) = transform(v)
operator fun Matrix3f.times(q: Quaternionfc) = rotate(q)
infix fun Matrix3f.rotate(q: Quaternionfc) = rotate(q)
infix fun Matrix3f.transform(v: Vector3f) = transform(v)

/* Matrix3d */

//operator fun Matrix3d.get(c: Int, r: Int): Double = get(c, r)
//operator fun Matrix3d.minus(m: Matrix3d) = sub(m)
//operator fun Matrix3d.plus(m: Matrix3dc) = add(m)
//operator fun Matrix3d.times(m: Matrix3dc) = mul(m)
//operator fun Matrix3d.times(m: Matrix3fc) = mul(m)
//operator fun Matrix3d.times(v: Vector3d) = transform(v)
//operator fun Matrix3d.times(v: Vector3f) = transform(v)
//operator fun Matrix3d.times(q: Quaternionfc) = rotate(q)
//operator fun Matrix3d.times(q: Quaterniondc) = rotate(q)
//infix fun Matrix3d.rotate(q: Quaternionfc) = rotate(q)
//infix fun Matrix3d.rotate(q: Quaterniondc) = rotate(q)
//infix fun Matrix3d.transform(v: Vector3f) = transform(v)
//infix fun Matrix3d.transform(v: Vector3d) = transform(v)

/* Matrix4x3f */

operator fun Matrix4x3f.get(c: Int, r: Int): Float = get(c, r)
operator fun Matrix4x3f.minus(m: Matrix4x3f) = sub(m)
operator fun Matrix4x3f.plus(m: Matrix4x3fc) = add(m)
operator fun Matrix4x3f.times(m: Matrix4x3fc) = mul(m)
operator fun Matrix4x3f.times(v: Vector4f) = transform(v)
operator fun Matrix4x3f.times(q: Quaternionfc) = rotate(q)
infix fun Matrix4x3f.rotate(q: Quaternionfc) = rotate(q)
infix fun Matrix4x3f.transform(v: Vector4f) = transform(v)
infix fun Matrix4x3f.transformPosition(v: Vector3f) = transformPosition(v)
infix fun Matrix4x3f.transformDirection(v: Vector3f) = transformDirection(v)

/* Matrix4x3d */

//operator fun Matrix4x3d.get(c: Int, r: Int): Double = get(c, r)
//operator fun Matrix4x3d.minus(m: Matrix4x3dc) = sub(m)
//operator fun Matrix4x3d.plus(m: Matrix4x3dc) = add(m)
//operator fun Matrix4x3d.times(m: Matrix4x3fc) = mul(m)
//operator fun Matrix4x3d.times(m: Matrix4x3dc) = mul(m)
//operator fun Matrix4x3d.times(v: Vector4d) = transform(v)
//operator fun Matrix4x3d.times(q: Quaternionfc) = rotate(q)
//operator fun Matrix4x3d.times(q: Quaterniondc) = rotate(q)
//infix fun Matrix4x3d.rotate(q: Quaternionfc) = rotate(q)
//infix fun Matrix4x3d.rotate(q: Quaterniondc) = rotate(q)
//infix fun Matrix4x3d.transform(v: Vector4d) = transform(v)
//infix fun Matrix4x3d.transformPosition(v: Vector3d) = transformPosition(v)
//infix fun Matrix4x3d.transformDirection(v: Vector3d) = transformDirection(v)

/* Matrix4f */

operator fun Matrix4f.get(c: Int, r: Int): Float = get(c, r)
operator fun Matrix4f.minus(m: Matrix4f) = sub(m)
operator fun Matrix4f.plus(m: Matrix4fc) = add(m)
operator fun Matrix4f.times(m: Matrix4fc) = mul(m)
operator fun Matrix4f.times(m: Matrix4x3fc) = mul(m, this)
operator fun Matrix4f.times(v: Vector4f) = transform(v)
operator fun Matrix4f.times(q: Quaternionfc) = rotate(q)
infix fun Matrix4f.mulAffine(m: Matrix4fc) = this.mulAffine(m)
infix fun Matrix4f.mulAffineR(m: Matrix4fc) = this.mulAffineR(m)
infix fun Matrix4f.rotate(q: Quaternionfc) = rotate(q)
infix fun Matrix4f.transform(v: Vector4f) = transform(v)
infix fun Matrix4f.transformPosition(v: Vector3f) = transformPosition(v)
infix fun Matrix4f.transformDirection(v: Vector3f) = transformDirection(v)

/* Matrix4d */

//operator fun Matrix4d.get(c: Int, r: Int): Double = get(c, r)
//operator fun Matrix4d.minus(m: Matrix4dc) = sub(m)
//operator fun Matrix4d.plus(m: Matrix4dc) = add(m)
//operator fun Matrix4d.times(m: Matrix4dc) = mul(m)
//operator fun Matrix4d.times(m: Matrix4x3fc) = mul(m, this)
//operator fun Matrix4d.times(m: Matrix4x3dc) = mul(m, this)
//operator fun Matrix4d.times(v: Vector4d) = transform(v)
//operator fun Matrix4d.times(q: Quaternionfc) = rotate(q)
//operator fun Matrix4d.times(q: Quaterniondc) = rotate(q)
//infix fun Matrix4d.mulAffine(m: Matrix4dc) = this.mulAffine(m)
//infix fun Matrix4d.mulAffineR(m: Matrix4dc) = this.mulAffineR(m)
//infix fun Matrix4d.rotate(q: Quaternionfc) = rotate(q)
//infix fun Matrix4d.transform(v: Vector4d) = transform(v)
//infix fun Matrix4d.transformPosition(v: Vector3d) = transformPosition(v)
//infix fun Matrix4d.transformDirection(v: Vector3f) = transformDirection(v)
//infix fun Matrix4d.transformDirection(v: Vector3d) = transformDirection(v)

/* Vector2f */

operator fun Vector2f.get(e: Int): Float = get(e)
operator fun Vector2f.minus(v: Vector2fc) = sub(v, Vector2f())
operator fun Vector2f.minus(v: Vector2f) = Vector2f(x - v.x, y - v.y)
operator fun Vector2f.plus(v: Vector2fc) = add(v, Vector2f())
operator fun Vector2f.plus(v: Float) = Vector2f(x + v, y + v)
operator fun Vector2f.unaryMinus() = negate()
operator fun Vector2f.div(v: Float) = Vector2f(x / v, y / v)

operator fun Vector2i.get(e: Int): Int = get(e)
operator fun Vector2i.minus(v: Vector2ic) = sub(v, Vector2i())
operator fun Vector2i.minus(v: Vector2i) = Vector2i(x - v.x, y - v.y)
operator fun Vector2i.plus(v: Vector2ic) = add(v, Vector2i())
operator fun Vector2i.plus(v: Int) = Vector2i(x + v, y + v)
operator fun Vector2i.unaryMinus() = negate()
operator fun Vector2i.div(v: Int) = Vector2i(x / v, y / v)

/* Vector2d */

//operator fun Vector2d.get(e: Int): Double = get(e)
//operator fun Vector2d.minus(v: Vector2fc) = sub(v)
//operator fun Vector2d.minus(v: Vector2dc) = sub(v)
//operator fun Vector2d.plus(v: Vector2fc) = add(v)
//operator fun Vector2d.plus(v: Vector2dc) = add(v)
//operator fun Vector2d.unaryMinus() = negate()

/* Vector3f */

//operator fun Vector3f.get(e: Int): Float = get(e)
operator fun Vector3f.minus(v: Vector3fc) = sub(v, Vector3f())

operator fun Vector3f.minus(v: Float) = Vector3f(x - v, y - v, z - v)

operator fun Vector2f.minus(v: Float) = sub(Vector2f(v, v), Vector2f())

//operator fun Vector3f.minus(other: Float) = sub(other, Vector3f())
operator fun Vector3f.plusAssign(other: Vector3f) {
  add(other)
}

operator fun Vector3f.plus(v: Vector3fc): Vector3f = Vector3f(x + v.x(), y + v.y(), z + v.z())
operator fun Vector3f.plus(v: Float): Vector3f = Vector3f(x + v, y + v, z + v)
operator fun Vector3f.unaryMinus() = negate()

/* Vector3d */

//operator fun Vector3d.get(e: Int): Double = get(e)
//operator fun Vector3d.minus(v: Vector3fc) = sub(v)
//operator fun Vector3d.minus(v: Vector3dc) = sub(v)
//operator fun Vector3d.plus(v: Vector3fc) = add(v)
//operator fun Vector3d.plus(v: Vector3dc) = add(v)
//operator fun Vector3d.unaryMinus() = negate()

/* Vector4f */

//operator fun Vector4f.get(e: Int): Float = get(e)
operator fun Vector4f.minus(v: Vector4fc) = sub(v)

operator fun Vector4f.plus(v: Vector4fc) = add(v)
operator fun Vector4f.unaryMinus() = negate()

/* Vector4d */

//operator fun Vector4d.get(e: Int): Double = get(e)
//operator fun Vector4d.minus(v: Vector4fc) = sub(v)
//operator fun Vector4d.minus(v: Vector4dc) = sub(v)
//operator fun Vector4d.plus(v: Vector4fc) = add(v)
//operator fun Vector4d.plus(v: Vector4dc) = add(v)
//operator fun Vector4d.unaryMinus() = negate()

/* Quaternionf */

operator fun Quaternionf.get(e: Int): Float = get(e)
operator fun Quaternionf.minus(q: Quaternionfc) = mul(q)
operator fun Quaternionf.unaryMinus() = conjugate()
operator fun Quaternionf.times(v: Vector3f) = transform(v, Vector3f())
operator fun Quaternionf.times(q: Quaternionf) = mul(q, Quaternion())
//operator fun Quaternionf.times(v: Vector4f) = transform(v)

/* Quaterniond */

//operator fun Quaterniond.get(e: Int): Double = get(e)
//operator fun Quaterniond.minus(q: Quaterniondc) = mul(q)
//operator fun Quaterniond.unaryMinus() = conjugate()
//operator fun Quaterniond.times(v: Vector3d) = transform(v)
//operator fun Quaterniond.times(v: Vector4d) = transform(v)


val Vector3f.xy: Vector2f
  get() = Vector2f(x, y)
