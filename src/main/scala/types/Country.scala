package types

case class Country(
    id: Long,
    name: String,
    continent: String,
    bestFood: Option[String],
    hasEiffelTower: Boolean
)
