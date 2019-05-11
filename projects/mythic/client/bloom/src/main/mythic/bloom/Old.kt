package mythic.bloom

data class SeedOld(
    val bag: StateBag = mapOf(),
    val bounds: Bounds,
    val clipBounds: Bounds? = null
)

typealias FlowerOld = (SeedOld) -> Blossom

data class FlatBox(
    val bounds: Bounds,
    val depiction: Depiction? = null,
    val clipBounds: Bounds? = null,
    val handler: Any? = null,
    val logic: LogicModule? = null
)

typealias FlatBoxes = List<FlatBox>
