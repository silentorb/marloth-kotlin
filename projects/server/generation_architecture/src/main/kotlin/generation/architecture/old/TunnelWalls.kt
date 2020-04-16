package generation.architecture.old

//val placeTunnelWalls: Architect = { config, realm, dice ->
//  tunnelNodes(realm.graph)
//      .flatMap { node ->
//        val segmentLength = 4f
//        val info = getTunnelInfo(realm.graph, node.id)
//        val lookAtAngle = getLookAtAngle(info.vector)
//        val halfWidth = standardTunnelWidth / 2f
//        val biome = config.biomes[node.biome]!!
//        val series = newFlushSeries(info.length, segmentLength)
//        listOf(-1f, 1f)
//            .filter { wallPlacementFilter(dice, biome) }
//            .map { sideMod ->
//              val items = series.flushItems.map { offset ->
//                Pair(offset, setOf(MeshAttribute.placementWall))
//              }
//                  .plus(series.fillerItems.map { offset ->
//                    Pair(offset, setOf(MeshAttribute.placementWallFiller))
//                  })
//              items.flatMap { (offset, meshFilter) ->
//                //                val randomFlip = if (dice.getBoolean()) 1 else -1
//                val sideOffset = Vector3(info.vector.y, -info.vector.x, 0f) * sideMod * halfWidth
//                val wallPosition = info.start + info.vector * offset + sideOffset
//                val wallAngle = lookAtAngle + sideMod * Pi / 2f
//                val mesh = dice.takeOne(filterMeshes(config.meshes, biome, meshFilter))
//                newWall(config, listOf(mesh), node, wallPosition, wallAngle)
//              }
//            }.flatten()
//      }
//}
