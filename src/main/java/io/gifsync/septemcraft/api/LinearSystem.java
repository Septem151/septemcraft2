package io.gifsync.septemcraft.api;

import java.util.Optional;

/**
 * A set of linear equations in as many unknowns, solved by Gaussian elimination with partial
 * pivoting. It is the arithmetic under a nodal analysis and nothing more: its rows and columns are
 * positions, its coefficients are numbers, and it knows nothing of what any of them stand for.
 */
final class LinearSystem
{
	private final int size;

	private final double[][] coefficients;

	private final double[] constants;

	/** Builds a system of the given number of equations, every coefficient starting at nothing. */
	LinearSystem(int size)
	{
		if (size < 0)
		{
			throw new IllegalArgumentException("A system holds no fewer than no equations, not " + size);
		}

		this.size = size;
		this.coefficients = new double[size][size];
		this.constants = new double[size];
	}

	/** A system carrying everything another already has, which solving this one leaves untouched. */
	private LinearSystem(LinearSystem other)
	{
		this.size = other.size;
		this.coefficients = new double[other.size][];
		for (int row = 0; row < other.size; row++)
		{
			this.coefficients[row] = other.coefficients[row].clone();
		}

		this.constants = other.constants.clone();
	}

	/**
	 * A system holding what this one holds, to be stamped further and solved on its own. Everything
	 * a circuit writes down that does not change between passes is written once and copied here,
	 * rather than written again every pass.
	 */
	LinearSystem copy()
	{
		return new LinearSystem(this);
	}

	/** Adds to the coefficient an unknown carries in an equation. */
	void add(int equation, int unknown, double value)
	{
		coefficients[equation][unknown] += value;
	}

	/** Adds to the constant an equation is set equal to. */
	void addConstant(int equation, double value)
	{
		constants[equation] += value;
	}

	/**
	 * The value of every unknown, or nothing when the system does not determine them - a pivot too
	 * small to divide by, or an answer that is not a number, which is a circuit that does not
	 * determine its own answer rather than arithmetic wearing out.
	 *
	 * <p>Eliminating rewrites the equations in place, so a system answers this once. Ask a
	 * {@link #copy()} for a second answer.
	 */
	Optional<double[]> solution()
	{
		double[][] rows = coefficients;
		double[] values = constants;
		for (int column = 0; column < size; column++)
		{
			int pivot = largestBelow(rows, column);
			if (Math.abs(rows[pivot][column]) < ElectricalConstants.SINGULARITY_THRESHOLD)
			{
				return Optional.empty();
			}

			swap(rows, values, column, pivot);
			eliminateBelow(rows, values, column);
		}

		double[] solved = substituteBack(rows, values);
		for (double value : solved)
		{
			if (!Double.isFinite(value))
			{
				return Optional.empty();
			}
		}

		return Optional.of(solved);
	}

	/** The row at or below a column's own whose entry in that column is the largest. */
	private int largestBelow(double[][] rows, int column)
	{
		int largest = column;
		for (int row = column + 1; row < size; row++)
		{
			if (Math.abs(rows[row][column]) > Math.abs(rows[largest][column]))
			{
				largest = row;
			}
		}

		return largest;
	}

	/** Exchanges two equations, which reorders nothing about what they say. */
	private static void swap(double[][] rows, double[] values, int one, int other)
	{
		double[] row = rows[one];
		rows[one] = rows[other];
		rows[other] = row;

		double value = values[one];
		values[one] = values[other];
		values[other] = value;
	}

	/** Takes a multiple of a pivot equation from every equation below it, clearing that column. */
	private void eliminateBelow(double[][] rows, double[] values, int column)
	{
		for (int row = column + 1; row < size; row++)
		{
			double factor = rows[row][column] / rows[column][column];
			if (factor == 0.0)
			{
				continue;
			}

			for (int entry = column; entry < size; entry++)
			{
				rows[row][entry] -= factor * rows[column][entry];
			}

			values[row] -= factor * values[column];
		}
	}

	/** Reads the unknowns off a triangular system, last one first. */
	private double[] substituteBack(double[][] rows, double[] values)
	{
		double[] solved = new double[size];
		for (int row = size - 1; row >= 0; row--)
		{
			double total = values[row];
			for (int column = row + 1; column < size; column++)
			{
				total -= rows[row][column] * solved[column];
			}

			solved[row] = total / rows[row][row];
		}

		return solved;
	}
}
