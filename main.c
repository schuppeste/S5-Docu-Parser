/*This file is part of S5Parser.
    S5Parser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.
    S5Parser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with S5Parser.  If not, see <http://www.gnu.org/licenses/>.
    Diese Datei ist Teil von S5Parser.
    S5Parser ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder jeder neueren
    veröffentlichten Version, weiter verteilen und/oder modifizieren.
    S5Parser wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.
    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <cstring>
#include <iostream>
#include <list>
#include <map>
#include <regex>
#include <iterator>
#include <string>
#include <sqlite3.h> 
#include <algorithm>
#include <time.h>
#include <unistd.h>
#include "main.h"
#include <fstream>
using namespace std;
static const string HEADER1     = "<!DOCTYPE html><html lang=\"de\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>S5Parser</title><style>\n";
static const string STYLEZUORD  = "pre{\nfont-size: 14px;\n\n} \na:link { background: lightgrey; text-decoration: none; color: black; }\na:visited { background: lightgrey; text-decoration: none; color: black; }\n\n/* The navigation bar */\n.navbar {\n    overflow: hidden;\n    background-color:  lightgrey;\n    position: fixed; /* Set the navbar to fixed position */\n    bottom: 0; /* Position the navbar at the top of the page */\n    width: 100%;\n    height:4%;\n    z-index:10;\n}\n\n/* Links inside the navbar */\n.navbar a {\n    float: left;\n    display: block;\n    color: #red;\n    text-align: center;\n    padding: 10px 16px;\n    text-decoration: none;\n}\nbody{\nmargin:0;\npadding:0;\n}\n/* Change background on mouse-over */\n.navbar a:hover {\n    background: #888888;\n    color: black;\n}\n.right{\n\nright: 0;\nwidth:50%;\nheight:100%;\noverflow-y: none;\nposition: fixed;\n\n}\npre{\nmargin-left:10px;\n}\n.Zuordnung {\n\noverflow-y: scroll;\nbackground: #EEEEEE;\nheight:96%;\n}\n/* Main content */\n.split{\nwidth:50%;\nheight:100%;\noverflow-y: scroll;\nposition: fixed;\n}\n.container{\nwidth:100%;\nheight:100%;\npadding:0;\n\n}\n\n.main {\nleft:0;\n\n   }";
static const string HEADER2     = "</style></head><body>\n\n<div class=\"container\">\n<div class=\"main split\"><pre><a href=\"#\" id=\"bausteine\"></a>";
static const string CONTENT     = "</pre></div>\n<div class=\"right\">\n<div class=\"Zuordnung\" ><pre>";
static const string FOOTER      = "</pre></div>\n<div tabindex=\"1\" class=\"navbar\"><a href=\"#ZLE\" tabindex=\"10\" onclick=\"\">Eingänge</a><a href=\"#ZLA\" tabindex=\"10\" onclick=\"\">Ausgänge</a><a href=\"#ZLM\" tabindex=\"10\" onclick=\"\">Merker</a><a href=\"#ZLS\" tabindex=\"10\" onclick=\"\">Ersatzmerker</a><a href=\"#ZPB\" tabindex=\"10\" >Bausteine</a><a href=\"#ZPT\" tabindex=\"10\" >Timer</a><a href=\"#ZPZ\" tabindex=\"10\" >Zähler</a></div>\n</div></div></body></html>";
static const string STYLESINGLE = "pre{\nfont-size: 14px;\nmargin-left:10px;\n}\n.tooltip {\n    position: relative;\n  display: inline-block;\n}\n\n.tooltip .tooltiptext {\n    visibility: hidden;\n    /*width: 60px;*/\n    background-color: #555;\n    color: #fff;\n   text-align: center;\n   padding: 5px 0;\n    border-radius: 6px;\n\n    /* Position the tooltip text */\n    position: absolute;\n    z-index: 1;\n    bottom: 125%;\n    left: 50%;\n    margin-left: 0px;\n\n    /* Fade in tooltip */\n    opacity: 0;\n    transition: opacity 0.3s;\n}\n\na:link { background: lightgrey; text-decoration: none; color: black; }\na:visited { background: lightgrey; text-decoration: none; color: black; }\n\n.navbar {\n    overflow: hidden;\n    background-color:  lightgrey;\n    position: fixed; /* Set the navbar to fixed position */\n    bottom: 0; /* Position the navbar at the top of the page */\n    width: 100%;\n    height:4%;\n    z-index:10;\n}\n\n.navbar a {\n    float: left;\n    display: block;\n    color: #red;\n    text-align: center;\n    padding: 10px 16px;\n    text-decoration: none;\n}\n\nbody{\nmargin:0;\npadding:0;\n}\n\n.navbar a:hover {\n    background: #888888;\n    color: black;\n}\n\n.right{\ndisplay:none;\nright: 0;\nwidth:0%;\nheight:100%;\noverflow-y: none;\nposition: fixed;\n}\n\n.Zuordnung {\noverflow-y: scroll;\nbackground: #EEEEEE;\nheight:96%;\n}\n\n.split{\n\nheight:100%;\noverflow-y: scroll;\nposition: fixed;\n}\n\n.container{\nwidth:100%;\nheight:100%;\npadding:0;\n}\n\n.main {\nleft:0;\nwidth: 100%;\n}\n";
static string fileoutput        = HEADER1;

int loadOrSaveDb(sqlite3 *pInMemory, const char *zFilename, int isSave);
string networks_get_links(int pb, int nw, string eam);
int db_select_match_insert(int pb, int nw, string eamname);
string networks_search_eam(int pb, int nw, string s);
int cb_networks(void *NotUsed, int argc, char **argv, char **azColName);
int callback(void *NotUsed, int argc, char **argv, char **azColName);

sqlite3 *db; //unclosed Memory Database
char *zErrMsg = 0; //DB Error Var 
int rc;
static string newHtml = "";
char *file1;
char *file2;
char *file3;
bool single=false;

static string getNetworksTable() {
    int lastpb;
    string networks = "";
    char query[] = "select pb,nw,code FROM networks ORDER BY pb,nw;";
    sqlite3_stmt *stmt;
    int rc = sqlite3_prepare_v2(db, query, -1, &stmt, NULL);


    while ((rc = sqlite3_step(stmt)) == SQLITE_ROW) {
        char* s = (char*) sqlite3_column_text(stmt, 2);
        // int length=sprintf(NULL, "<a href=\"#\" id=\"pb%dnw%d\" ></a>", sqlite3_column_int(stmt, 0), sqlite3_column_int(stmt, 1)); /* 3 */

        string nwlink = "<a href=\"#\" id=\"pb";
        nwlink.append(to_string(sqlite3_column_int(stmt, 0)));
        nwlink.append("nw");
        nwlink.append(to_string(sqlite3_column_int(stmt, 1)));
        nwlink.append("\" ></a>"); /* 3 */
        //string nwlink = "<a href=\"#\" id=\"" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\"></a>"
        //tempnw = tempnw.replaceAll("(.*Netzwerk.*)", "<h4>PB" + rs.getInt("pb") + " $1 </h4>");
        regex exp("(.*Netzwerk.*)", std::regex::ECMAScript);
        string stemp = s;


        string replacestring = "<h4>PB";
        replacestring.append(to_string(sqlite3_column_int(stmt, 0)));
        replacestring.append(" $1 </h4>");
        stemp = std::regex_replace(stemp, exp, replacestring);

        string pblink = "<a href=\"#\" id=\"pb";
        pblink.append(to_string(sqlite3_column_int(stmt, 1)));
        pblink.append("\"></a>");
        if (lastpb != sqlite3_column_int(stmt, 0)) {
            networks.append(pblink);
        }
        lastpb = sqlite3_column_int(stmt, 0);
        networks.append(nwlink);
        networks.append(networks_search_eam(sqlite3_column_int(stmt, 0), sqlite3_column_int(stmt, 1), stemp));
    }

    sqlite3_finalize(stmt);
    return networks;
}

string create_Network_Link(int pb, int nw, int text) {
    return 0;
}

string networks_search_eam(int pb, int nw, string s) {

    string pattern[] = {
        "([E][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([A][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([S][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([M][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([T][ ]{1,3}[0-9]{1,3})",
        "([Z][ ]{1,3}[0-9]{1,3})"
    };

    string network = s;
    for (int n = 0; n < 4; n++) {
        regex rgx(pattern[n], std::regex::ECMAScript);
        auto words_begin =
                std::sregex_iterator(s.begin(), s.end(), rgx);
        auto words_end = std::sregex_iterator();

        for (std::sregex_iterator i = words_begin; i != words_end; ++i) {
            std::smatch match = *i;
            std::string matcher = match.str();
            regex exp(matcher, std::regex::ECMAScript);
            network = std::regex_replace(network, exp, networks_get_links(pb, nw, matcher));

        }

    }

    return network;
}

string networks_get_title(string eam) {
    string titleQuery = "select eamname,pb,GROUP_CONCAT(nw) AS nw FROM  (SELECT eamname,pb,nw FROM refs ORDER BY nw) WHERE eamname='";
    titleQuery.append(eam);
    titleQuery.append("' GROUP BY pb ORDER BY pb");
    int rc;
    const char* query = titleQuery.c_str();
    sqlite3_stmt *stmt;
    rc = sqlite3_prepare_v2(db, query, -1, &stmt, NULL);
    string ret;
    while ((rc = sqlite3_step(stmt)) == SQLITE_ROW) {
        char* pbtext = (char*) sqlite3_column_text(stmt, 2);
        ret.append("PB");
        ret.append(to_string(sqlite3_column_int(stmt, 1)));
        ret.append(": ");
        ret.append(string(pbtext));
        ret.append("
");

    }

    sqlite3_finalize(stmt);

    return ret;
}

string networks_get_links(int pb, int nw, string eam) {
    int rc;
    //cout << eam << endl;
    char query[200];
    regex exprRMHead2(" ", regex::ECMAScript);
    std::string eamclear = regex_replace(eam, exprRMHead2, "");
    std::string eamnbsp = regex_replace(eam, exprRMHead2, " ");
    const char* temp = eamclear.c_str();
    sprintf(query, "select nw,pb,eamname FROM refs WHERE eamname='%s' AND( (nw>%d  AND pb=%d) OR ( pb>%d))  ORDER BY pb,nw LIMIT 1;", eamclear.c_str(), nw, pb, pb); /* 3 */
    sqlite3_stmt *stmt;
    rc = sqlite3_prepare_v2(db, query, -1, &stmt, NULL);


    char ret[1000];
    if ((rc = sqlite3_step(stmt)) == SQLITE_ROW) {
        sprintf(ret, "<a href=\"#pb%dnw%d\" title=\"%s\" >%s</a>", sqlite3_column_int(stmt, 1), sqlite3_column_int(stmt, 0), networks_get_title(eamclear).c_str(), eamnbsp.c_str()); /* 3 */
        //cout << "FOUND:" << sqlite3_column_text(stmt, 2) << endl;

    } else {
        sprintf(query, "select nw,pb,eamname FROM refs WHERE eamname='%s' ORDER BY pb,nw", eamclear.c_str()); /* 3 */
        sqlite3_stmt *stmt2;
        int rc = sqlite3_prepare_v2(db, query, -1, &stmt2, NULL);
        if ((rc = sqlite3_step(stmt2)) == SQLITE_ROW) {
            sprintf(ret, "<a href=\"#pb%dnw%d\" title=\"%s\" >%s</a>", sqlite3_column_int(stmt2, 1), sqlite3_column_int(stmt2, 0), " ", eamnbsp.c_str()); /* 3 */
        } else {
            sprintf(ret, "%s", eam);
        }
        sqlite3_finalize(stmt);
        sqlite3_finalize(stmt2);
    }


    string returns = ret;
    return returns;
}

string zuordnungen_get_links(string eam) {
    int rc;
    char query[200];
    regex exprRMHead2("[ ]+", regex::ECMAScript);
    std::string eamclear = regex_replace(eam, exprRMHead2, "");
    std::string eamnbsp = regex_replace(eam, exprRMHead2, " ");
    const char* temp = eamclear.c_str();
    sprintf(query, "select nw,pb,eamname FROM refs WHERE eamname='%s' ORDER BY pb,nw LIMIT 1;", temp); /* 3 */
    sqlite3_stmt *stmt;
    rc = sqlite3_prepare_v2(db, query, -1, &stmt, NULL);
    char ret[1000];
    if ((rc = sqlite3_step(stmt)) == SQLITE_ROW) {
        sprintf(ret, "<a href=\"#pb%dnw%d\" title=\"%s\" >%s</a>", sqlite3_column_int(stmt, 1), sqlite3_column_int(stmt, 0), networks_get_title(eamclear).c_str(), eamnbsp.c_str()); /* 3 */
    } else {
        return eamnbsp;
    }
    sqlite3_finalize(stmt);
    return string(ret);
}

int db_select_match_insert(int pb, int nw, string eamname) {
    sqlite3_stmt *stmt;
    const char* p_c_str = eamname.c_str();

    const string check = "INSERT INTO refs ( id,eamname,pb,nw) SELECT ?1,?4,?2,?3 WHERE NOT EXISTS(SELECT 1 FROM refs WHERE pb =" + std::to_string(pb) + " AND nw=" + std::to_string(nw) + " AND eamname = '" + eamname + "');";

    const char* p_c_insert = check.c_str();
    sqlite3_prepare_v2(db, p_c_insert, -1, &stmt, NULL);
    sqlite3_bind_int(stmt, 1, 0);
    sqlite3_bind_int(stmt, 2, pb); /* 2 */
    sqlite3_bind_int(stmt, 3, nw);
    sqlite3_bind_text(stmt, 4, p_c_str, -1, SQLITE_STATIC); /* 3 */

    rc = sqlite3_step(stmt);
    if (rc != SQLITE_DONE) {
        printf("ERROR inserting data: %s\n", sqlite3_errmsg(db));

    }
    return 0;
}

int insertrefs(int pb, int nw, string code) {

    string patterns[] = {
        "([S|E|A|M][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "[ ]+([T|Z][ ]{1,3}[0-9]{1,3})[ ]",
        "([D|F|P][B|W|X][ ]{0,5}[0-9]{1,3})(([ ]|[^ ]))"
    };

    for (int n = 0; n < 2; n++) {
        const string input = code;
        regex rgx(patterns[n], std::regex::ECMAScript);
        sregex_iterator it(input.begin(), input.end(), rgx);
        sregex_iterator reg_end;
        regex exprRMHead2(" ", regex::ECMAScript);
        for (; it != reg_end; ++it) {
            std::string matcher = it->str();
            matcher = regex_replace(matcher, exprRMHead2, "");
            db_select_match_insert(pb, nw, matcher);

        }
    }
    return 0;

}

int insertnw(int pb, int nw, string code) {
    sqlite3_stmt *stmt;
    const char* p_c_str = code.c_str();
    sqlite3_prepare_v2(db, "INSERT INTO networks ( id,pb,nw,code) values (?1,?2,?3,?4);", -1, &stmt, NULL);
    sqlite3_bind_int(stmt, 1, 0);
    sqlite3_bind_int(stmt, 2, pb); /* 2 */
    sqlite3_bind_int(stmt, 3, nw);
    sqlite3_bind_text(stmt, 4, p_c_str, -1, SQLITE_STATIC); /* 3 */

    rc = sqlite3_step(stmt);
    if (rc != SQLITE_DONE) {
        printf("ERROR inserting data: %s\n", sqlite3_errmsg(db));

    }
    int rc;
    char *sql;
    const char* data = "Callback function called";
    sqlite3_finalize(stmt);
    return 0;
}

int createTablesAndDB() {

    rc = sqlite3_open(":memory:", &db);

    if (rc) {
        fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
        return (0);
    } else {
        fprintf(stderr, "Opened database successfully\n");
    }
    const char* CreateQuery = "CREATE TABLE IF NOT EXISTS refs(id int auto_increment,eamname varchar(255),pb int, nw int)";
    const char* CreateQuery2 = "CREATE TABLE IF NOT EXISTS networks(id int auto_increment,pb int,nw int, code longvarchar(20000))";

    rc = sqlite3_exec(db, CreateQuery, callback, 0, &zErrMsg);
    rc = sqlite3_exec(db, CreateQuery2, callback, 0, &zErrMsg);

    if (rc != SQLITE_OK) {
        fprintf(stderr, "SQL error: %s\n", zErrMsg);
        sqlite3_free(zErrMsg);
    } else {
        fprintf(stdout, "Table created successfully\n");
    }
    return 0;
}

static int mycallback(void *data, int argc, char **argv, char **azColName) {
    int i;
    fprintf(stderr, "%s: ", (const char*) data);

    for (i = 0; i < argc; i++) {
        printf("%s = %s\n", azColName[i], argv[i] ? argv[i] : "NULL");
    }

    printf("\n");
    return 0;
}

int readnetworks(int pb, string s) {
    //std::cout << "Aufruf" << endl;

    string clear = "";

    regex exprRMHead1{"[ ]+Blatt[ ]+[0-9]{1,3}[ \\r\\n]", std::regex::ECMAScript};
    s = regex_replace(s, exprRMHead1, clear);

    regex exprRMHead2("[ ]{0,3}(PB[ ]{1,3}[0-9]).*LAE.*", regex::ECMAScript);
    s = regex_replace(s, exprRMHead2, clear);

    smatch m;
    regex e("Netzwerk[ ]{1,3}([0-9]{1,3})[ ]+[0-9A-Z]{1,4}", regex::ECMAScript); //regex expr{"^[ ]+Blatt[ 0-9]+$",};
    //std::cout << output;
    sregex_iterator it(s.begin(), s.end(), e);
    sregex_iterator reg_end;
    string oldmatch = "";

    long startNW = 0;
    long newNWpos = 0;
    std::string lastNWtemp;

    regex rgx("Netzwerk[ ]{1,3}([0-9]{1,3})", std::regex::ECMAScript);
    for (; it != reg_end; ++it) {


        newNWpos = it->position();
        const string network = s.substr(startNW, newNWpos - startNW);

        // std::cout << network << std::endl;


        startNW = newNWpos;
        std::smatch match;
        int networknumber = 0;

        if (std::regex_search(network.begin(), network.end(), match, rgx)) {
            networknumber = std::stoi(match[1]);
            //std::cout << match[0] << std::endl;

        }
        //std::cout << networknumber << std::endl;
        insertnw(pb, networknumber, network);
        insertrefs(pb, networknumber, network);
    }
    std::smatch match;
    int networknumber = 0;
    int NW = 0;
    const string network = s.substr(startNW);
    if (std::regex_search(network.begin(), network.end(), match, rgx)) {
        networknumber = std::stoi(match[1]);
    }
    insertnw(pb, networknumber, network);
    return 0;
}

int createHtml() {
    return 0; //newHtml += htmlmenu;
}

string prepare_network_out(int pb, int nw, string network) {
    return 0;
}

string zuordnungen() {
    long length;
    FILE * f = fopen("SCHIFZLS.INI", "rb");
    char *buffer = 0;
    if (f) {

        fseek(f, 0, SEEK_END);
        length = ftell(f);
        fseek(f, 0, SEEK_SET);
        buffer = (char *) malloc(length);
        if (buffer) {
            fread(buffer, 1, length, f);
        }
        fclose(f);
    }

    if (buffer) {
        //printf(buffer);
    }

    string clear = "";

    string s = buffer;

    regex exprRMHead1{".*Datei.*Blatt[ ]+[0-9]{1,4}.*", std::regex::ECMAScript};
    s = regex_replace(s, exprRMHead1, clear);

    regex exprRMHead2(".*Operand.*Symbol.*Kommentar.*", regex::ECMAScript);
    s = regex_replace(s, exprRMHead2, clear);
    regex exprRMHead3("([ ]+|>)([E][ ]{1,3}[0-9]{1,3}[.][0-7])", regex::ECMAScript);
    s = regex_replace(s, exprRMHead3, "<a href=\"\" id=\"ZLE\"><h4>Eingänge</h4></a><br><br>$0", std::regex_constants::format_first_only);
    regex exprRMHead4("([ ]+|>)([A][ ]{1,3}[0-9]{1,3}[.][0-7])", regex::ECMAScript);
    s = regex_replace(s, exprRMHead4, "<a href=\"\" id=\"ZLA\"><h4>Ausgänge</h4></a><br><br>$0", std::regex_constants::format_first_only);
    regex exprRMHead5("([ ]+|>)([S][ ]{1,3}[0-9]{1,3}[.][0-7])", regex::ECMAScript);
    s = regex_replace(s, exprRMHead5, "<a href=\"\" id=\"ZLS\"><h4>Ersatzmerker</h4></a><br><br>$0", std::regex_constants::format_first_only);
    regex exprRMHead6("([ ]+|>)([M][ ]{1,3}[0-9]{1,3}[.][0-7])", regex::ECMAScript);
    s = regex_replace(s, exprRMHead6, "<a href=\"\" id=\"ZLM\"><h4>Merker</h4></a><br><br>$0", std::regex_constants::format_first_only);
    regex exprRMHead7("([ ]+|>)([P][B][ ]{1,3}[0-9]{1,3})[ ]", regex::ECMAScript);
    s = regex_replace(s, exprRMHead7, "<a href=\"\" id=\"ZPB\"><h4>Bausteine</h4></a><br><br>$0", std::regex_constants::format_first_only);
    regex exprRMHead8("([ ]+|>)([T][ ]{1,3}[0-9]{1,3})[ ]", regex::ECMAScript);
    s = regex_replace(s, exprRMHead8, "<a href=\"\" id=\"ZPT\"><h4>Timer</h4></a><br><br>$0", std::regex_constants::format_first_only);
    regex exprRMHead9("[P][B][ ]{1,3}([0-9]{1,3})[ ]", regex::ECMAScript);
    s = regex_replace(s, exprRMHead9, "<a href=\"#pb$1\" >PB$1</a>", std::regex_constants::format_first_only);
    string patterns[] = {
        "([E][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([A][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([S][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "([M][ ]{1,3}[0-9]{1,3}[.][0-7])",
        "[ ]+([T|Z][ ]{1,3}[0-9]{1,3})[ ]",
        "([D|F|P][B|W|X][ ]{0,5}[0-9]{1,3})(([ ]|[^ ]))"
    };
    string input = s;
    for (int n = 0; n < 5; n++) {
        regex rgx(patterns[n], std::regex::ECMAScript);
        sregex_iterator it(s.begin(), s.end(), rgx);
        sregex_iterator reg_end;

        for (; it != reg_end; ++it) {
            std::string matcher = it->str();
            regex exprRMHead2(matcher, regex::ECMAScript);
            input = regex_replace(input, exprRMHead2, zuordnungen_get_links(matcher));
        }
    }
    return input;
};


int main(int argc, char* argv[]) {

    //  rc = sqlite3_open(":memory:", &db);
    // loadOrSaveDb(db, "/tmp/sq3test.db",0);
    // cout<<zuordnungen();
  
    if (argc < 2&& argc>4) {
        cout << "Zuwenig Argumente!!" << endl << "ConvertS5.exe PBDATEI ZUDATEI AUSGABEDATEI" << endl << "Ohne Zuordnungsdatei 0 eintragen";
        return 0;
    }
    if(argc==3){
    single=true;
    file1 = argv[1];
    file2 = argv[2];
    }else
    {
    file1 = argv[1];
    file2 = argv[2];
    file3 = argv[3];
    }
    long length;
    FILE * f = fopen(file1, "rb");
    char *buffer = 0;
    if (f) {

        fseek(f, 0, SEEK_END);
        length = ftell(f);
        fseek(f, 0, SEEK_SET);
        buffer = (char *) malloc(length);
        if (buffer) {
            fread(buffer, 1, length, f);
        }
        fclose(f);
    }

    if (buffer) {
        //printf(buffer);

    }

    createTablesAndDB();

    // </editor-fold>
    regex expr{"^.*Blatt[ ]+[0-9]{1,3}.*$", regex::ECMAScript};
    string formatter = "";

    string output = regex_replace(buffer, expr, formatter);
    regex matcher2("<"); //"<",

    formatter = "<";
    output = regex_replace(output, matcher2, formatter);

    regex matcher3(">"); //"<",
    formatter = ">";
    output = regex_replace(output, matcher3, formatter);

    std::string s = output;

    smatch m;
    regex e("[ ]{0,3}(PB[ ]{1,3}[0-9]).*LAE.*", regex::ECMAScript); //regex expr{"^[ ]+Blatt[ 0-9]+$",};

    sregex_iterator it(s.begin(), s.end(), e);
    sregex_iterator reg_end;
    string oldmatch = "";

    long startpb = 0;
    long newpb = 0;
    regex rgx("[ ]{0,3}PB[ ]{1,3}([0-9]{1,3})", std::regex::ECMAScript);
    for (; it != reg_end; ++it) {

        if (oldmatch == it->str()) {

            const std::string st = it->str();

        } else {

            newpb = it->position();
            const string networks = s.substr(startpb, newpb);
            std::smatch match;
            int pbtemp = 0;

            if (std::regex_search(networks.begin(), networks.end(), match, rgx)) {
                pbtemp = std::stoi(match[1]);
                readnetworks(pbtemp, networks);
            }
            startpb = it->position();
        }
        oldmatch = it->str();
    }
    std::smatch match;
    int pbtemp = 0;
    const string networks = s.substr(startpb);
    if (std::regex_search(networks.begin(), networks.end(), match, rgx)) {
        pbtemp = std::stoi(match[1]);
    }


   
    if(single)
        fileoutput.append(STYLESINGLE);
    else
        fileoutput.append(STYLEZUORD);
    
    fileoutput.append(HEADER2);
    fileoutput.append(getNetworksTable());
    fileoutput.append(CONTENT);
    fileoutput.append(zuordnungen());
    fileoutput.append(FOOTER);



    ofstream myfile;
    if(single)
    myfile.open(file2);
    else
    myfile.open(file3);    
    if (myfile) {
        myfile << fileoutput;
        myfile.close();
    } else {
        std::cerr << "Failure opening " << argv[3] << '\n';
        return -1;
    }
    
    return 0;
}

int callback(void *NotUsed, int argc, char **argv, char **azColName) {

    cout << argv[2] << "NW" << argv[3] << "EAM" << argv[1] << endl;
    return 0;
}

int cb_networks(void *NotUsed, int argc, char **argv, char **azColName) {

    cout << networks_search_eam(atoi(argv[1]), atoi(argv[2]), argv[3]);
    return 0;
}

static int cb_match(void *NotUsed, int argc, char **argv, char **azColName) {
    int i;
    for (i = 0; i < argc; i++) {
        printf("%s = %s\n", azColName[i], argv[i] ? argv[i] : "NULL");
    }
    printf("\n");
    return 0;

}

int loadOrSaveDb(sqlite3 *pInMemory, const char *zFilename, int isSave) {
    int rc; /* Function return code */
    sqlite3 *pFile; /* Database connection opened on zFilename */
    sqlite3_backup *pBackup; /* Backup object used to copy data */
    sqlite3 *pTo; /* Database to copy to (pFile or pInMemory) */
    sqlite3 *pFrom; /* Database to copy from (pFile or pInMemory) */

    /* Open the database file identified by zFilename. Exit early if this fails
     ** for any reason. */
    rc = sqlite3_open(zFilename, &pFile);
    if (rc == SQLITE_OK) {

        /* If this is a 'load' operation (isSave==0), then data is copied
         ** from the database file just opened to database pInMemory. 
         ** Otherwise, if this is a 'save' operation (isSave==1), then data
         ** is copied from pInMemory to pFile.  Set the variables pFrom and
         ** pTo accordingly. */
        pFrom = (isSave ? pInMemory : pFile);
        pTo = (isSave ? pFile : pInMemory);

        /* Set up the backup procedure to copy from the "main" database of 
         ** connection pFile to the main database of connection pInMemory.
         ** If something goes wrong, pBackup will be set to NULL and an error
         ** code and message left in connection pTo.
         **
         ** If the backup object is successfully created, call backup_step()
         ** to copy data from pFile to pInMemory. Then call backup_finish()
         ** to release resources associated with the pBackup object.  If an
         ** error occurred, then an error code and message will be left in
         ** connection pTo. If no error occurred, then the error code belonging
         ** to pTo is set to SQLITE_OK.
         */
        pBackup = sqlite3_backup_init(pTo, "main", pFrom, "main");
        if (pBackup) {
            (void) sqlite3_backup_step(pBackup, -1);
            (void) sqlite3_backup_finish(pBackup);
        }
        rc = sqlite3_errcode(pTo);
    }

    /* Close the database connection opened on database file zFilename
     ** and return the result of this function. */
    (void) sqlite3_close(pFile);
    return rc;
}
