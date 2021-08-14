package marloth.definition.templates

//fun newTreasureChest(meshInfo: MeshInfoMap, position: Vector3, amount: Int): Hand {
//  val shape = meshInfo[MeshId.treasureChest.toString()]!!.shape
//  return Hand(
//      body = Body(
//          position = position
//      ),
//      collisionShape = if (shape != null)
//        CollisionObject(
//            shape = shape,
//            groups = CollisionGroups.static,
//            mask = CollisionGroups.staticMask
//        )
//      else
//        null,
//      depiction = Depiction(
//          type = DepictionType.staticMesh,
//          mesh = MeshId.treasureChest.toString()
//      ),
////      interactable = Interactable(
////          primaryCommand = WidgetCommand(
////              text = TextId.gui_take,
////              action = TakeItem()
////          )
////      ),
////      resources = ResourceBundle(
////          values = mapOf(
////              ResourceId.money.name to amount
////          )
////      )
//  )
//}
