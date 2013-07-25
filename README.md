r2linkauditor
=============

# pageLinkAuditor.sh
is a spider script specifically for checking links in moved domains.


## Output

* a directory called links-<yyyy>-<mm>-<dd>T<hh>-<mm>, which will hold a file for each page spidered
These files show categories of links for a particular page, which are prefixed by a number for easy grepping:
1. Links to URLs neither in original domain or new domain
2.

* spidering activity will be logged to audit.out.<yyyy>-<mm>-<dd>T<hh>-<mm>
This shows status of linked URLs and which renderer renders them.
It also shows redirects and reports redirect chains.


## Usage

A typical use of the script is:
./page-link-auditor.sh www.guardian.co.uk www.theguardian.com / 3
which would follow links from www.theguardian.com/ to a depth of 3.


To use a proxy, eg to simulate connecting from US:
./page-link-auditor.sh www.guardian.co.uk www.theguardian.com /culture 2 ec2-xyz.com:8080


To look for specific text in pages, eg 'umbrella':
./page-link-auditor.sh www.guardian.co.uk www.theguardian.com /culture 2 ec2-xyz.com:8080 umbrella
