from setuptools import setup

setup(name='expose-to-the-light',
      version='0.1',
      description='Controls camera settings for timelapse',
      url='https://github.com/szigyi/expose-to-the-light',
      author='Szigyi',
      author_email='szigyi@example.com',
      license='MIT',
      packages=['ettl'],
      install_requires=[
          'scipy',
      ],
      zip_safe=False)