"""
A temporary file to make ubuntu-setup.sh work
"""

import optparse
import os
import errno
import sys
import collections
import re

from datetime import date
from pyang import plugin, util, error


def pyang_plugin_init():
    """Registers an instance of the jnc plugin"""
    plugin.register_plugin(JRCPlugin())


class JRCPlugin(plugin.PyangPlugin):
    """The plug-in class of JNC.

    The methods of this class are invoked by pyang during initialization. The
    emit method is of particular interest if you are new to writing plugins to
    pyang. It is from there that the parsing of the YANG statement tree
    emanates, producing the generated classes that constitutes the output of
    this plug-in.

    """

    def __init__(self):
        self.done = set([])  # Helps avoiding processing modules more than once

    def add_output_format(self, fmts):
        """Adds 'jnc' as a valid output format and sets the format to jnc if
        the -d/--jnc-output option is set, but -f/--format is not.

        """
        self.multiple_modules = False
        fmts['jrc'] = self

        args = sys.argv[1:]

    def add_opts(self, optparser):
        """Adds options to pyang, displayed in the pyang CLI help message"""
        optlist = []
        g = optparser.add_option_group('JRC output specific options')
        g.add_options(optlist)

