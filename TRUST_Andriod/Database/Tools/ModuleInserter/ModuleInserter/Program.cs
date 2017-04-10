using System;
using System.IO;
using System.Text;

namespace ModuleInserter
{
	class MainClass
	{
		private static string directory = "/home/nathan/Documents/CptS421/Modules/Module_09";
		private static string outFileName = "Insert" + DateTime.Now.Millisecond + ".sql";
		private static string mnumber = "9";

		public static void Main (string[] args)
		{
			string[] files = Directory.GetFiles (directory);

			if (files.Length < 1) {
				Console.WriteLine ("No files in given directory. Did you mess up the path?");
				return;
			}

			Console.WriteLine ("\nFiles Found:");
			foreach (string file in files) {
				Console.WriteLine (file);
			}

			Console.WriteLine ("Creating Insert Statements");
			string format = "INSERT INTO page (mnumber, length, version, content, title, subtitle) VALUES ({0}, {1}, 1, '{2}', '{3}', '{4}');\n";

			FileStream outFile = File.Open(string.Format ("{0}/{1}", directory, outFileName), FileMode.Create);
			foreach (string file in files) {
				if (file.EndsWith(".sql"))
				    continue;

				FileStream inFile = File.Open (file, FileMode.Open);
				byte[] fileData = new byte[65535];
				int bytesRead = inFile.Read (fileData, 0, fileData.Length);
				inFile.Close ();

				string text = Encoding.ASCII.GetString (fileData, 0, bytesRead);

				string title = text.Substring (0, text.IndexOf ("\n"));
				title = title.Substring(title.IndexOf(":") + 1);
				title = title.Replace ("'", "''");

				text = text.Substring (text.IndexOf ("\n") + 1);

				string subtitle = text.Substring (0, text.IndexOf ("\n"));
				subtitle = subtitle.Substring(subtitle.IndexOf(":") + 1);
				subtitle = subtitle.Replace ("'", "''");

				text = text.Substring (text.IndexOf ("\n") + 1);
				text = text.Replace ("'", "''");

				string fileDataString = string.Format(format, mnumber, bytesRead, text, title, subtitle);		// Convert to string to drop any extra null bytes on the end of fileData
				byte[] writeBytes = Encoding.ASCII.GetBytes (fileDataString);

				outFile.Write (writeBytes, 0, writeBytes.Length);
			}

			Console.WriteLine ("Done.");
		}
	}
}
