package pl.otros.logview.util;

import com.google.common.base.Splitter;
import org.apache.commons.lang.NotImplementedException;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;
import org.unix4j.unix.cut.CutOptionSet_cf;
import org.unix4j.util.Range;

public class UnixProcessing {


  public String processText(String text, String commandLine) {
    final String[] split = commandLine.split("\\|");
    final Unix4jCommandBuilder builder = buildUnixCli(Unix4j.fromString(text), split, 0);
    return builder.toStringResult();
  }

  protected Unix4jCommandBuilder buildUnixCli(Unix4jCommandBuilder builder, String[] commands, int position) {
    if (commands.length == position) {
      return builder;
    } else {
      final String command = commands[position].trim();
      final int endIndex = command.indexOf(' ');
      String cli = command.substring(0, endIndex);
      String args = command.substring(endIndex).trim();

      if (cli.equals("grep")) {
        final Unix4jCommandBuilder grep = builder.grep(args.split("\\s+?"));
        return buildUnixCli(grep, commands, ++position);
      } else if (cli.equals("cut")) {
        final String[] split = args.split("\\s+?");
        if (args.startsWith("-c")) {
          final String range = args.substring(2).trim();
          if (range.matches("\\d+?-\\d+?")) {
            int from = Integer.parseInt(range.replaceAll("-.*", ""));
            int to = Integer.parseInt(range.replaceAll(".*-", ""));
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_c, Range.between(from, to));
            return buildUnixCli(cut, commands, ++position);
          } else if (range.matches("\\d+?-")) {
            int to = Integer.parseInt(range.replaceAll("-.*", ""));
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_c, Range.toEndFrom(to));
            return buildUnixCli(cut, commands, ++position);
          } else if (range.matches("-\\d+?")) {
            int from = Integer.parseInt(range.replaceAll(".*-", ""));
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_c, Range.fromStartTo(from));
            return buildUnixCli(cut, commands, ++position);
          } else if (range.matches("\\d[\\d,]+")) {
            final int[] indexes = Splitter.on(',')
              .trimResults()
              .splitToList(range)
              .stream()
              .map(Integer::parseInt)
              .mapToInt(i -> i).toArray();
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_c, indexes);
            return buildUnixCli(cut, commands, ++position);
          }
        } else if (args.startsWith("-d")) {
          final String delimiter = args.substring(2).trim().split(" ")[0];
          final String t1 = args.substring(args.indexOf("-f")).trim();
          final String t2 = t1.replace("-f", "");
          final String range = t2.trim();
          if (range.matches("\\d+?-\\d+?")) {
            int from = Integer.parseInt(range.replaceAll("-.*", ""));
            int to = Integer.parseInt(range.replaceAll(".*-", ""));
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_f, delimiter, Range.between(from, to));
            return buildUnixCli(cut, commands, ++position);
          } else if (range.matches("\\d+?-")) {
            int to = Integer.parseInt(range.replaceAll("-.*", ""));
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_f, delimiter, Range.toEndFrom(to));
            return buildUnixCli(cut, commands, ++position);
          } else if (range.matches("-\\d+?")) {
            int from = Integer.parseInt(range.replaceAll(".*-", ""));
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_f, delimiter, Range.fromStartTo(from));
            return buildUnixCli(cut, commands, ++position);
          } else if (range.matches("\\d[\\d,]*")) {
            final int[] indexes = Splitter.on(',')
              .trimResults()
              .splitToList(range)
              .stream()
              .map(Integer::parseInt)
              .mapToInt(i -> i).toArray();
            final Unix4jCommandBuilder cut = builder.cut(CutOptionSet_cf.Active_f, delimiter, indexes);
            return buildUnixCli(cut, commands, ++position);
          }
        }
        final Unix4jCommandBuilder cut = builder.cut(split);
        return buildUnixCli(cut, commands, ++position);
      } else if (cli.equals("sed")) {
        final Unix4jCommandBuilder sed = builder.sed(args.split("\\s+?"));
        return buildUnixCli(sed, commands, ++position);
      }
      throw new NotImplementedException("Cli " + cli + " is not supported");
    }
  }
}
