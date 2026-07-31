class SortDefinition {
  final String expression;
  final String name;
  final String ascendingText;
  final String descendingText;
  final bool initialAscendingSort;

  const SortDefinition({
    required this.expression,
    required this.name,
    required this.ascendingText,
    required this.descendingText,
    this.initialAscendingSort = true,
  });
}

class Sort {
  final String expression;
  final String name;
  final bool ascending;

  const Sort(this.expression, this.name, this.ascending);

  Sort sortAscending() {
    return Sort(expression, name, true);
  }

  Sort sortDescending() {
    return Sort(expression, name, false);
  }

  String apiString() {
    return '$expression,${ascending ? 'asc' : 'desc'}';
  }
}
