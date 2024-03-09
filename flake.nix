{
  description = "PDQ";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    mvn2nix.url = "github:fzakaria/mvn2nix";
  };

  outputs = { self, nixpkgs, flake-utils, mvn2nix, ... }@inputs:
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          # import nix packages
          pkgs = import nixpkgs {
            inherit system;
          };
         
          # maven repository of dependencies
          # mavenRepository = mvn2nix.buildMavenRepositoryFromLockFile
          #   {
          #     file = ./mvn2nix-lock.json;
          #   };
          
          # Utility to run a script easily in the flakes app
          simple_script = name: add_deps: text: let
            exec = pkgs.writeShellApplication {
              inherit name text;
              runtimeInputs = with pkgs; [
                jdk11
                maven
              ] ++ add_deps;
            };
          in {
            type = "app";
            program = "${exec}/bin/${name}";
          };

        in with pkgs;
          {
            ###################################################################
            #                             scripts                             #
            ###################################################################
            apps = {
              default = simple_script "create_mvn_lockfile" [] ''
                     nix run github:fzakaria/mvn2nix#mvn2nix > mvn2nix-lock.json 
              '';

            };

            ###################################################################
            #                             build jar                           #
            ###################################################################
            packages = {              
              pdq =  stdenv.mkDerivation rec {
                name = "PDQ";
                nativeBuildInputs = [ makeWrapper ];
                buildInputs = [ maven ];                
                src = ./.;
                buildPhase = ''
                  mvn package -DskipTests -Dmaven.repo.local=$out
               '';
                installPhase = ''
    mkdir -p $out/bin $out/share/pdq
    install -Dm644 pdq-main/target/pdq-main-2.0.0-jar-with-dependencies.jar $out/share/pdq/pdq.jar

    makeWrapper ${jre}/bin/java $out/bin/pdq --add-flags "-jar $out/share/pdq/pdq.jar"
  '';                  
              };
             
              # default = self.packages.pdq;
            };

            
            ###################################################################
            #                       development shell                         #
            ###################################################################
            devShells.default =
              mkShell
                {
                  nativeBuildInputs = with pkgs; [
                    jdk11
                    maven
                    python311
                  ];
                };
          }
      );
}
