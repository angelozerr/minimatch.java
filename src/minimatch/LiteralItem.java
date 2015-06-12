package minimatch;


public class LiteralItem extends ParseItem
{
	public LiteralItem(String source)
	{
		//setSource(source);
	}
	/*@Override
	public String RegexSource(Options options)
	{
		return Regex.Escape(getSource());
	}
	@Override
	public boolean Match(String input, Options options)
	{
		return input.equals(getSource(), options.getNoCase() ? StringComparison.OrdinalIgnoreCase : StringComparison.Ordinal);
	}*/

}
